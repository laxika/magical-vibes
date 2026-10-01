package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "2")
@CardRegistration(set = "BLC", collectorNumber = "102")
public class HazelOfTheRootbloom extends Card {

    public HazelOfTheRootbloom() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(2),
                        new TapMultiplePermanentsCost(new XValue(), new PermanentIsTokenPredicate(), true),
                        new AwardAnyColorManaEffect(
                                new XValue(), ManaSpendRestriction.NONE, null, false, true)
                ),
                "{T}, Pay 2 life, Tap X untapped tokens you control: Add X mana in any combination of colors."
        ));

        PermanentIsTokenPredicate token = new PermanentIsTokenPredicate();
        PermanentHasSubtypePredicate squirrel = new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL);
        target(new ControlledPermanentPredicateTargetFilter(
                token, "Target must be a token you control")).addEffect(
                EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                SequenceEffect.of(
                        new ConditionalEffect(
                                new TargetPermanentMatches(squirrel),
                                new CreateTokenCopyOfTargetPermanentEffect(new Fixed(2))),
                        new ConditionalEffect(
                                new NotCondition(new TargetPermanentMatches(squirrel)),
                                new CreateTokenCopyOfTargetPermanentEffect())
                ));
    }
}
