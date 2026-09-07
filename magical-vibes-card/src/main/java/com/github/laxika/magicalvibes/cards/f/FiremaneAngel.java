package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "205")
public class FiremaneAngel extends Card {

    public FiremaneAngel() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new ConditionalEffect(new SourceIsOnBattlefield(), new GainLifeEffect(1)),
                        "Gain 1 life?"));
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new MayEffect(
                        new ConditionalEffect(new SourceCardInGraveyard(), new GainLifeEffect(1)),
                        "Gain 1 life?"));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{6}{R}{R}{W}{W}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{6}{R}{R}{W}{W}: Return Firemane Angel from your graveyard to the battlefield. "
                        + "Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
