package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "LCI", collectorNumber = "123")
@CardRegistration(set = "LCI", collectorNumber = "338")
public class StarvingRevenant extends Card {

    public StarvingRevenant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SurveilThenEffect.direct(2, SequenceEffect.of(
                new DrawCardEffect(new EventValue()),
                new LoseLifeEffect(new Scaled(new EventValue(), 3), LoseLifeRecipient.CONTROLLER))));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new ConditionalEffect(
                new GraveyardCardThreshold(8, new CardIsPermanentPredicate()),
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                        new GainLifeEffect(1))));
    }
}
