package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Nighteyes the Desecrator — flipped face of {@link NezumiGraverobber}.
 * Legendary Creature — Rat Wizard 4/2.
 * {4}{B}: Put target creature card from a graveyard onto the battlefield under your control.
 */
public class NighteyesTheDesecrator extends Card {

    public NighteyesTheDesecrator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .build()),
                "{4}{B}: Put target creature card from a graveyard onto the battlefield under your control."
        ));
    }
}
