package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "9")
public class FirstSliversChosen extends Card {

    public FirstSliversChosen() {
        // First Sliver's Chosen is itself a Sliver, so ALL_OWN_CREATURES includes it when it
        // matches the filter. Each Sliver gets the exalted attack trigger.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureYouControlAttacks(),
                        TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                        BoostEffect.target(1, 1)),
                Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
