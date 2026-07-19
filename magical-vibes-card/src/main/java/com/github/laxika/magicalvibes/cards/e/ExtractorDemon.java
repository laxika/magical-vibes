package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "44")
public class ExtractorDemon extends Card {

    public ExtractorDemon() {
        // Whenever another creature leaves the battlefield, you may have target player mill two cards.
        // The "may" and the (any) player target are resolved on the stack via the MayEffect flow.
        addEffect(EffectSlot.ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD,
                new MayEffect(new MillEffect(2, MillRecipient.TARGET_PLAYER),
                        "have target player mill two cards"));

        // Unearth {2}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .grantHaste(true)
                        .exileAtEndStep(true)
                        .build()),
                "Unearth {2}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
