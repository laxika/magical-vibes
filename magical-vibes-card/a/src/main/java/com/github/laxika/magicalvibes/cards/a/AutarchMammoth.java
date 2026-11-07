package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "153")
public class AutarchMammoth extends Card {

    public AutarchMammoth() {
        CreateTokenEffect elephant = new CreateTokenEffect(
                "Elephant", 3, 3, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of());
        addTrigger(Triggers.enters(ObjectRelation.THIS), elephant);
        addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withEventCondition(new SourceIsSaddled()), elephant);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(5), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 5",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
