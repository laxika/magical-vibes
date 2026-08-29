package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "83")
public class TeferiMageOfZhalfir extends Card {

    public TeferiMageOfZhalfir() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.STATIC, new OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect());
    }
}
