package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INR", collectorNumber = "175")
public class ThroughTheBreach extends Card {

    public ThroughTheBreach() {
        // You may put a creature card from your hand onto the battlefield.
        // That creature gains haste. Sacrifice that creature at the beginning of the next end step.
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        "creature", false, false, true, true),
                "Put a creature card from your hand onto the battlefield?"
        ));

        // Splice onto Arcane {2}{R}{R}
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{2}{R}{R}"));
    }
}
