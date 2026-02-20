package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record WarpWorldEnchantmentPlacement(UUID controllerId, Card card, UUID attachmentTargetId) {
}
