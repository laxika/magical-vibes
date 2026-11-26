package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayGainControlOfCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "136")
public class InfernalDenizen extends Card {

    public InfernalDenizen() {
        // At the beginning of your upkeep, sacrifice two Swamps. If you can't, tap this creature,
        // and an opponent may gain control of a creature you control of their choice for as long
        // as this creature remains on the battlefield.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new ConditionalEffect(new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                        new SacrificePermanentsEffect(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP), Subjects.you())),
                new ConditionalEffect(new NotCondition(HappenedThisWay.atLeast(StepKind.SACRIFICE, 2)),
                        SequenceEffect.of(new TapPermanentsEffect(Subjects.source()), new OpponentMayGainControlOfCreatureYouControlEffect(Duration.WHILE_SOURCE_REMAINS)))));

        // {T}: Gain control of target creature for as long as this creature remains on the battlefield.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GainControlOfTargetEffect(Duration.WHILE_SOURCE_REMAINS)),
                "{T}: Gain control of target creature for as long as this creature remains on the battlefield.",
                TargetFilters.creature()));
    }
}
