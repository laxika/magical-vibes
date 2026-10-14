package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Engine-internal follow-up of a clash: each clashing player decides, in APNAP order, whether to
 * put the card they revealed on the bottom of their library, then all those cards move at the same
 * time (CR 701.30c). Inserted by the clash handlers right after the reveal; not used on cards.
 *
 * @param clashingPlayerIds the players who clashed, in APNAP order
 */
public record PlaceClashCardsEffect(List<UUID> clashingPlayerIds) implements CardEffect {
}
