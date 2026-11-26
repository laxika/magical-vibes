package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "76")
public class AetherSting extends Card {

    public AetherSting() {
        // Whenever an opponent casts a creature spell, this enchantment deals 1 damage to that player.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 1));
    }
}
