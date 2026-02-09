package com.github.laxika.magicalvibes.networking.message;

import java.util.List;

public record DeclareAttackersRequest(List<Integer> attackerIndices) {
}
