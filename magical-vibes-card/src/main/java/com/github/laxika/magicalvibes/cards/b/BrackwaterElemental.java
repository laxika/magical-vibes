package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "21")
public class BrackwaterElemental extends Card {

    public BrackwaterElemental() {
        // When this creature attacks or blocks, sacrifice it at the beginning of the next end step.
        addEffect(EffectSlot.ON_ATTACK, new SacrificeSelfAtEndStepEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeSelfAtEndStepEffect());

        // Unearth {2}{U}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step or if it would leave the battlefield.
        // Unearth only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .grantHaste(true)
                        .exileAtEndStep(true)
                        .build()),
                "Unearth {2}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
