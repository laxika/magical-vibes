package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TurfWarCombatDamageEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "54")
@CardRegistration(set = "NCC", collectorNumber = "154")
public class TurfWar extends Card {

    private static final PermanentPredicate CONTESTED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.CONTESTED)));

    public TurfWar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerChoosesLandAndPutCounterEffect(CounterType.CONTESTED));
        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanent(CONTESTED_LAND)),
                new TurfWarCombatDamageEffect(true));
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(),
                TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerControlsPermanent(CONTESTED_LAND)),
                new TurfWarCombatDamageEffect(false));
    }
}
