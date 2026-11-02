package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "87")
public class Embersmith extends Card {

    public Embersmith() {
        // Whenever you cast an artifact spell, you may pay {1}. If you do, Embersmith deals 1 damage to any target.
        // The target is chosen as the trigger goes on the stack; the {1} payment is offered on resolution.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ARTIFACT),
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{1}"
        ));
    }
}
