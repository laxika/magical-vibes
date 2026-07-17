package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "125")
public class SoulBarrier extends Card {

    public SoulBarrier() {
        // Whenever an opponent casts a creature spell, this enchantment deals 2 damage to that player unless they pay {2}.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new DamageUnlessPaysEffect(2, 2, new CardTypePredicate(CardType.CREATURE)));
    }
}
