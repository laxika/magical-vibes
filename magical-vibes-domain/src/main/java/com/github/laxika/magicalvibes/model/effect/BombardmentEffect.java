package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Map;
import java.util.UUID;

/**
 * Until end of turn, transforms the controller's nonbattlefield cards into red Missile sorceries.
 *
 * <p>It is also the payload of the floating effect its resolution makes on its controller, until end of turn:
 * {@code originalCards} maps each card it made a Missile, by id, to the card that card was before any Missile change
 * this turn. A card that changes zones becomes a new object without the change (CR 400.7) and leaves the map; the
 * cards still in it get their own characteristics back as the effect ends (CR 514.2). Empty on the card's own effect.
 */
public record BombardmentEffect(Map<UUID, Card> originalCards) implements CardEffect {

    public BombardmentEffect {
        originalCards = originalCards == null ? Map.of() : Map.copyOf(originalCards);
    }

    public BombardmentEffect() {
        this(Map.of());
    }
}
