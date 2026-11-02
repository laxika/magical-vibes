package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "125")
@CardRegistration(set = "ICE", collectorNumber = "103")
public class SoulBarrier extends Card {

    public SoulBarrier() {
        // Whenever an opponent casts a creature spell, this enchantment deals 2 damage to that player unless they pay {2}.
        addTrigger(Triggers.opponentCastsSpell(),
                new DamageUnlessPaysEffect(2, 2, new CardTypePredicate(CardType.CREATURE)));
    }
}
