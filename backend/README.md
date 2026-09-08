# OAuth Backend Contract

This folder documents the contract used by the Flutter client. Keep provider credentials on the backend.

Required HTTPS routes:

- `GET /auth/youtube/start`
- `GET /auth/facebook/start`
- `GET /auth/tiktok/start`

Each route should start the provider's official OAuth flow and return/redirect to a secure callback handled by the backend.

Do not place client secrets, access tokens, refresh tokens, private keys, page IDs or stream keys in the Flutter application or repository.
