package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "67")
public class HypervoltGrasp extends Card {

    public HypervoltGrasp() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new DealDamageToAnyTargetEffect(1)),
                                "{T}: This creature deals 1 damage to any target."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(ReturnToHandEffect.self()),
                "{1}{U}: Return this Aura to its owner's hand."));
    }
}
