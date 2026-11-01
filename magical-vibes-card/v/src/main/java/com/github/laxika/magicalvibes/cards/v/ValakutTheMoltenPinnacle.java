package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherThanTriggeringPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "228")
@CardRegistration(set = "ZNE", collectorNumber = "29")
@CardRegistration(set = "TLE", collectorNumber = "61")
public class ValakutTheMoltenPinnacle extends Card {

    public ValakutTheMoltenPinnacle() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                        new ConditionalEffect(
                                new ControlsOtherThanTriggeringPermanentCount(
                                        5, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                                new MayEffect(
                                        new DealDamageToAnyTargetEffect(3),
                                        "Deal 3 damage to any target?"))));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
