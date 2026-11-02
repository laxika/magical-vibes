package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "43")
@CardRegistration(set = "SLD", collectorNumber = "2005")
@CardRegistration(set = "TLE", collectorNumber = "9")
public class AgentOfTreachery extends Card {

    public AgentOfTreachery() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentNotPredicate(
                        new PermanentOwnedBySourceControllerPredicate())),
                new DrawCardEffect(3)));
    }
}
