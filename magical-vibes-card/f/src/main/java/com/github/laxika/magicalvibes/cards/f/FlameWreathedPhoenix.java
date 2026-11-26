package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "97")
public class FlameWreathedPhoenix extends Card {

    public FlameWreathedPhoenix() {
        addStaticEffect(new TributeEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TributeNotPaidEffect(SequenceEffect.of(
                new SetSelfKeywordIndefinitelyEffect(Keyword.HASTE, true),
                new GrantEffectToTargetEffect(new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                        new PutCardsIntoHandEffect(Subjects.sourceCard())), Duration.PERMANENT, false,
                        Subjects.source()))));
    }
}
