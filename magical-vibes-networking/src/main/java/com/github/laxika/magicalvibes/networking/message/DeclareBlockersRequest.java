package com.github.laxika.magicalvibes.networking.message;

import java.util.List;

public record DeclareBlockersRequest(Long gameId, List<BlockerAssignment> blockerAssignments) {
}
