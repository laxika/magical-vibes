package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "76")
@CardRegistration(set = "FDN", collectorNumber = "325")
public class VengefulBloodwitch extends Card {

    public VengefulBloodwitch() {
        var drainTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        drainTarget.addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                new GainLifeEffect(1)));
    }
}
