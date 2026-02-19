package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.Map;

public record SubmitDeckRequest(List<Integer> cardIndices, Map<String, Integer> basicLands) {
}
