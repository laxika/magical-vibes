package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceCardToughnessAtLeast;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "9")
public class DiminishedReturner extends Card {

    public DiminishedReturner() {
        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {B}{B}: Diminished Returner perpetually gets -1/-1, then return it to the battlefield.
        // Activate only if Diminished Returner is in your graveyard and its toughness is 2 or greater.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(
                        new PerpetuallyBoostCardEffect(this, -1, -1),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "{B}{B}: Diminished Returner perpetually gets -1/-1, then return it to the battlefield. "
                        + "Activate only if Diminished Returner is in your graveyard and its toughness is 2 or greater."
        ).withActivationCondition(
                new SourceCardToughnessAtLeast(2),
                "Activate only if Diminished Returner is in your graveyard and its toughness is 2 or greater."
        ));
    }
}
