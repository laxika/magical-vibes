--liquibase formatted sql
--changeset magical-vibes:review-001
CREATE TABLE review_card (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    class_name TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL
);
CREATE TABLE review_printing (
    card_id INTEGER NOT NULL REFERENCES review_card(id),
    set_code TEXT NOT NULL,
    collector_number TEXT NOT NULL,
    PRIMARY KEY (card_id, set_code, collector_number)
);
CREATE TABLE review_run (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    model TEXT NOT NULL,
    reasoning_effort TEXT NOT NULL,
    catalog_commit TEXT,
    created_at TEXT NOT NULL
);
CREATE TABLE review_settings (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    active_run_id INTEGER REFERENCES review_run(id)
);
INSERT INTO review_settings(id) VALUES (1);
CREATE TABLE review_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id INTEGER NOT NULL REFERENCES review_run(id),
    card_id INTEGER NOT NULL REFERENCES review_card(id),
    class_name TEXT NOT NULL,
    source_path TEXT NOT NULL,
    set_code TEXT NOT NULL,
    collector_number TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED','RUNNING','COMPLETED','FAILED')),
    current_attempt TEXT,
    UNIQUE (run_id, card_id)
);
CREATE TABLE review_attempt (
    token TEXT PRIMARY KEY,
    task_id INTEGER NOT NULL REFERENCES review_task(id),
    worker_id TEXT NOT NULL,
    started_at TEXT NOT NULL,
    finished_at TEXT,
    outcome TEXT CHECK (outcome IN ('PASS','FINDINGS','ERROR')),
    reviewed_commit TEXT,
    publication_status TEXT CHECK (publication_status IN ('NOT_REQUIRED','PUSHED','FAILED')),
    published_commit TEXT,
    execution_error TEXT,
    publication_error TEXT,
    result_json TEXT
);
CREATE TABLE review_finding (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    attempt_token TEXT NOT NULL REFERENCES review_attempt(token),
    description TEXT NOT NULL
);
CREATE INDEX review_task_queue ON review_task(run_id, status, class_name);
CREATE INDEX review_task_card ON review_task(card_id, run_id);
CREATE INDEX review_attempt_task ON review_attempt(task_id, started_at);
CREATE INDEX review_finding_attempt ON review_finding(attempt_token);
