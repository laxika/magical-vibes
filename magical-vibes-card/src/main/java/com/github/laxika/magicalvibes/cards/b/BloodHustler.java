package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "80")
public class BloodHustler extends Card {

    public BloodHustler() {
        addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                new OncePerTurnTriggerEffect(new PutCountersOnSourceEffect(1, 1, 1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                        new GainLifeEffect(1)
                ),
                "{3}{B}: Target opponent loses 1 life and you gain 1 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
