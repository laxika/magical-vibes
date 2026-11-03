package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "247")
@CardRegistration(set = "SLD", collectorNumber = "1356")
@CardRegistration(set = "SPG", collectorNumber = "28")
@CardRegistration(set = "SLC", collectorNumber = "6")
@CardRegistration(set = "SLC", collectorNumber = "33")
public class FieldOfTheDead extends Card {

    public FieldOfTheDead() {
        addStaticEffect(new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // Whenever this land or another land you control enters, if you control seven or more lands with different
        // names, create a 2/2 black Zombie creature token.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL))),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsDistinctPermanentNamesCount(7, new PermanentIsLandPredicate())),
                List.of(new EffectRegistration(CreateTokenEffect.blackZombie(1)))));
    }
}
