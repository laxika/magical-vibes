package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "108")
@CardRegistration(set = "TLE", collectorNumber = "189")
public class LoAndLiRoyalAdvisors extends Card {

    public LoAndLiRoyalAdvisors() {
        PutCountersEffect counterEffect =
                PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentHasSubtypePredicate(CardSubtype.ADVISOR));
        addTrigger(Triggers.opponentDiscardsCard(), counterEffect);
        addTrigger(new TriggerCondition(Triggers.mills(null, ObjectRelation.OPPONENT_OWNS)),
                TriggerOptions.ONE_OR_MORE, counterEffect);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U/B}",
                List.of(new MillEffect(4, Subjects.targetPlayer())),
                "{2}{U/B}: Target player mills four cards."
        ));
    }
}
