package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.AiDifficulty;

public record CreateDraftRequest(String draftName, String setCode, int aiCount, AiDifficulty aiDifficulty) {
}
