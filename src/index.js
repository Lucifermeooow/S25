const GOOGLE_TOKEN_URL =
  "https://oauth2.googleapis.com/token";

const GOOGLE_API_URL =
  "https://www.googleapis.com/youtube/v3/liveBroadcasts";

const GOOGLE_CLIENT_ID =
  "18650771866-userdlenr0c5ldfe6tqollqet84m0rv2.apps.googleusercontent.com";

const GOOGLE_REDIRECT_URI =
  "https://stream-v21-anti.impossible7man.workers.dev/auth/youtube/callback";

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (url.pathname === "/health") {
      return Response.json({
        ok: true,
        service: "stream-v21-anti",
      });
    }

    if (url.pathname === "/auth/youtube/live-status") {
      if (!env.OAUTH_KV) {
        return Response.json(
          {
            ok: false,
            error: "OAUTH_KV is missing",
          },
          { status: 500 }
        );
      }

      /*
       * Use the account created during the previous
       * Google OAuth test.
       */
      const accountId =
        "a2bcfb91-7b07-4de5-ab8c-b2aa8e5d9af9";

      const stored =
        await env.OAUTH_KV.get(
          `youtube_account:${accountId}`
        );

      if (!stored) {
        return Response.json(
          {
            ok: false,
            error: "YouTube account not found",
          },
          { status: 404 }
        );
      }

      const account =
        JSON.parse(stored);

      if (!account.refreshToken) {
        return Response.json(
          {
            ok: false,
            error: "Refresh token not found",
          },
          { status: 400 }
        );
      }

      /*
       * Get a fresh access token.
       */
      const tokenBody =
        new URLSearchParams();

      tokenBody.set(
        "client_id",
        GOOGLE_CLIENT_ID
      );

      tokenBody.set(
        "client_secret",
        env.GOOGLE_CLIENT_SECRET
      );

      tokenBody.set(
        "refresh_token",
        account.refreshToken
      );

      tokenBody.set(
        "grant_type",
        "refresh_token"
      );

      const tokenResponse =
        await fetch(
          GOOGLE_TOKEN_URL,
          {
            method: "POST",
            headers: {
              "content-type":
                "application/x-www-form-urlencoded",
            },
            body:
              tokenBody.toString(),
          }
        );

      const tokenData =
        await tokenResponse.json();

      if (!tokenResponse.ok) {
        return Response.json(
          {
            ok: false,
            provider: "youtube",
            stage: "token_refresh",
            error: tokenData,
          },
          { status: 400 }
        );
      }

      const accessToken =
        tokenData.access_token;

      /*
       * Ask YouTube for the authenticated
       * user's live broadcasts.
       */
      const apiUrl =
        new URL(GOOGLE_API_URL);

      apiUrl.searchParams.set(
        "part",
        "id,snippet,status"
      );

      apiUrl.searchParams.set(
        "mine",
        "true"
      );

      apiUrl.searchParams.set(
        "maxResults",
        "10"
      );

      const youtubeResponse =
        await fetch(
          apiUrl.toString(),
          {
            headers: {
              Authorization:
                `Bearer ${accessToken}`,
            },
          }
        );

      const youtubeData =
        await youtubeResponse.json();

      if (!youtubeResponse.ok) {
        return Response.json(
          {
            ok: false,
            provider: "youtube",
            stage: "youtube_api",
            error: youtubeData,
          },
          { status: youtubeResponse.status }
        );
      }

      return Response.json({
        ok: true,
        provider: "youtube",
        authenticated: true,
        liveApi: true,
        broadcasts:
          youtubeData.items ?? [],
      });
    }

    return new Response(
      "Not Found",
      { status: 404 }
    );
  },
};
