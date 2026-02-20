package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

public record LibraryBottomReorderRequest(UUID playerId, List<Card> cards) {
}
