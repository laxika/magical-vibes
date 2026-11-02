package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "7")
@CardRegistration(set = "DMC", collectorNumber = "83")
public class ManaCannons extends Card {

    public ManaCannons() {
        // Whenever you cast a multicolored spell, this enchantment deals damage equal to that spell's colors to any target.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(new DealDamageToAnyTargetEffect(new TriggeringSpellColorCount()))
        ));
    }
}
