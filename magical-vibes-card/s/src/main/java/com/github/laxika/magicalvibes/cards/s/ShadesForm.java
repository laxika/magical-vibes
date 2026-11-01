package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "81")
public class ShadesForm extends Card {

    public ShadesForm() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{B}",
                                List.of(new BoostSelfEffect(1, 1)),
                                "{B}: This creature gets +1/+1 until end of turn."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));

        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
