package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "65")
public class BrackishTrudge extends Card {

    public BrackishTrudge() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{1}{B}: Return this card from your graveyard to your hand. Activate only if you gained life this turn."
        ).withActivationCondition(
                new GainedLifeThisTurn(),
                "Activate only if you gained life this turn."
        ));
    }
}
