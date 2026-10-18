package com.github.laxika.magicalvibes.networking.model;
import com.github.laxika.magicalvibes.model.DeckFormat;
import java.util.*;
/**
 * Command zones and commander state. {@code revealableMissionIds} are the viewer's Secret Mission cards
 * they may reveal right now.
 */
public record CommanderView(DeckFormat format, Map<UUID, List<CardView>> commandZones,
        Map<UUID, List<UUID>> commanders, Map<UUID, Integer> tax,
        Map<UUID, Map<UUID, Integer>> damageReceived, List<UUID> playableCardIds,
        List<UUID> revealableMissionIds) {}
