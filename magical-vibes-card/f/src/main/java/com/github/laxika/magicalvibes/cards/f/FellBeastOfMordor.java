package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "LTC", collectorNumber = "513")
@CardRegistration(set = "LTC", collectorNumber = "557")
public class FellBeastOfMordor extends Card {

    public FellBeastOfMordor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        var targetOpponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        var counters = new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE);
        var loseLife = new LoseLifeEffect(counters, LoseLifeRecipient.TARGET_PLAYER);
        var gainLife = new GainLifeEffect(counters);

        target(targetOpponent)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, loseLife)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, gainLife)
                .addEffect(EffectSlot.ON_ATTACK, loseLife)
                .addEffect(EffectSlot.ON_ATTACK, gainLife);
    }
}
