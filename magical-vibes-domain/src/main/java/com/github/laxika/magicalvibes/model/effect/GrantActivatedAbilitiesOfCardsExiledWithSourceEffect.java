package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: the permanents {@code subject} names that match {@code filter} have all activated abilities, mana
 * abilities included, of the cards of type {@code cardType} exiled with the source (CR 607.2a): "Foods you control have
 * all activated abilities of all creature cards exiled with this creature" (Hazel's Brewmaster), Steward of the
 * Harvest's land cards, Agatha's Soul Cauldron's creatures with +1/+1 counters.
 */
public record GrantActivatedAbilitiesOfCardsExiledWithSourceEffect(
        CardType cardType, PermanentSubject subject, PermanentPredicate filter) implements CardEffect {
}
