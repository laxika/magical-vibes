package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.Map;

public record DeclareAttackersRequest(List<Integer> attackerIndices, Map<Integer, String> attackTargets) {
}
