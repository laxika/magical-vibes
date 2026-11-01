package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "88")
public class AlBhedSalvagers extends Card {

    private static final SequenceEffect DRAIN = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
            new GainLifeEffect(1));

    public AlBhedSalvagers() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));

        addTrigger(Triggers.dies(ObjectRelation.THIS), DRAIN);
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()), DRAIN));
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new TriggeringArtifactControllerConditionalEffect(DRAIN));
    }
}
