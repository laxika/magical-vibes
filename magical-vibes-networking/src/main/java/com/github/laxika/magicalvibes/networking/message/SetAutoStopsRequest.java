package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.List;

public record SetAutoStopsRequest(List<TurnStep> stops) {
}
