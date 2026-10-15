package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "WAR", collectorNumber = "155")
public class BondOfFlourishing extends Card {

    public BondOfFlourishing() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(3, new CardIsPermanentPredicate()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
