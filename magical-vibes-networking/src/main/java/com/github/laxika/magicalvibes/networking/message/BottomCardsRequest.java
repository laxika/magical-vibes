package com.github.laxika.magicalvibes.networking.message;

import java.util.List;

public record BottomCardsRequest(Long gameId, List<Integer> cardIndices) {
}
