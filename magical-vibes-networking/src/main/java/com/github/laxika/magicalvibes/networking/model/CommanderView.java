package com.github.laxika.magicalvibes.networking.model;
import com.github.laxika.magicalvibes.model.DeckFormat;
import java.util.*;
public record CommanderView(DeckFormat format, Map<UUID, List<CardView>> commandZones,
        Map<UUID, List<UUID>> commanders, Map<UUID, Integer> tax,
        Map<UUID, Map<UUID, Integer>> damageReceived, List<UUID> playableCardIds) {}
