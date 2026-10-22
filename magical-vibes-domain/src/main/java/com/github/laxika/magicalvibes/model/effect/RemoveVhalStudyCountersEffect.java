package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * The "When this creature specializes" trigger of Vhal, Eager Scholar's specialized face: removes
 * all study counters from the creature and then queues the face's "When you do" follow-up.
 */
public record RemoveVhalStudyCountersEffect(CardColor color) implements CardEffect {
}
