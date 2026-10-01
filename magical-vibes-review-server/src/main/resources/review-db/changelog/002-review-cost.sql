--liquibase formatted sql
--changeset magical-vibes:review-002
ALTER TABLE review_attempt ADD COLUMN input_tokens INTEGER CHECK (input_tokens >= 0);
ALTER TABLE review_attempt ADD COLUMN cached_input_tokens INTEGER CHECK (cached_input_tokens >= 0 AND cached_input_tokens <= input_tokens);
ALTER TABLE review_attempt ADD COLUMN output_tokens INTEGER CHECK (output_tokens >= 0);
ALTER TABLE review_attempt ADD COLUMN estimated_cost_usd NUMERIC CHECK (estimated_cost_usd >= 0);
