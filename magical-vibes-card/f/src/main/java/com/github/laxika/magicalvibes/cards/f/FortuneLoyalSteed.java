package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndSaddledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "12")
@CardRegistration(set = "SLD", collectorNumber = "830")
public class FortuneLoyalSteed extends Card {

    public FortuneLoyalSteed() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsSaddled()),
                new DelayedEndOfCombatEffect(new ExileSelfAndSaddledCreatureEffect()));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(1), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 1",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
