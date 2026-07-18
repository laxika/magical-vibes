package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "196")
public class Mulch extends Card {

    public Mulch() {
        // Reveal the top cards, put all lands into hand, rest into graveyard. With
        // chooseCount == lookCount every revealed land auto-moves to hand — no choice is offered.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                4, 4, new CardTypePredicate(CardType.LAND), true));
    }
}
