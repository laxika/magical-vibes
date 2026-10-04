package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "168")
public class BalustradeWurm extends Card {

    public BalustradeWurm() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, CounterType.FINALITY)
                ),
                "{2}{G}{G}: Return this card from your graveyard to the battlefield with a finality counter on it. "
                        + "Activate only if there are four or more card types among cards in your graveyard and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new Delirium(),
                "Activate only if there are four or more card types among cards in your graveyard."
        ));
    }
}
