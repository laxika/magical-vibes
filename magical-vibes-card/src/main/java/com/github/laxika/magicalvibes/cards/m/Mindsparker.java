package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Mindsparker — 3/2 first striker that punishes white and blue instants and sorceries:
 * "Whenever an opponent casts a white or blue instant or sorcery spell, this creature deals
 * 2 damage to that player."
 */
@CardRegistration(set = "M14", collectorNumber = "146")
public class Mindsparker extends Card {

    public Mindsparker() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastDamageToCasterEffect(2,
                new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.WHITE),
                                new CardColorPredicate(CardColor.BLUE))),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))))));
    }
}
