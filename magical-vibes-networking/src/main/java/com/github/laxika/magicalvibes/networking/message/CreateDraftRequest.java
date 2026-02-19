package com.github.laxika.magicalvibes.networking.message;

public record CreateDraftRequest(String draftName, String setCode, int aiCount) {
}
