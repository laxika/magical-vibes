package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "13")
public class SamiteBlessing extends Card {

    public SamiteBlessing() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(PreventDamageFromChosenSourceEffect.nextDamageToTargetCreature()),
                                "{T}: The next time a source of your choice would deal damage to target creature this turn, prevent that damage.",
                                TargetFilters.creature()
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));
    }
}
