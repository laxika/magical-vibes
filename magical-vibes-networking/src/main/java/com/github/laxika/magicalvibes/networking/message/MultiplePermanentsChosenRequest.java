package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.UUID;

public record MultiplePermanentsChosenRequest(List<UUID> permanentIds) {
}
