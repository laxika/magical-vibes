package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GeodeGrotto;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "146")
public class DowsingDevice extends Card {

    public DowsingDevice() {
        setBackFaceCard(new GeodeGrotto());

        CardEffect transformIfFourArtifacts = new ConditionalEffect(
                new AllOf(List.of(new ControlsPermanentCount(4, new PermanentIsArtifactPredicate()))),
                new TransformSelfEffect());

        CardEffect enterEffect = SequenceEffect.of(new BoostTargetCreatureEffect(1, 0),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET), transformIfFourArtifacts);
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addTriggeredAbility(new TriggeredAbility(List.of(
                                new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                                new TriggerCondition(Triggers.enters(Triggers.ARTIFACT,
                                        ObjectRelation.ANOTHER_YOU_CONTROL))),
                        TriggerOptions.DEFAULT, List.of(new EffectRegistration(enterEffect))));
    }

    @Override
    public String getBackFaceClassName() {
        return "GeodeGrotto";
    }
}
