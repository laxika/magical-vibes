package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledTokensEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "10")
@CardRegistration(set = "DMC", collectorNumber = "86")
public class CadricSoulKindler extends Card {

    public CadricSoulKindler() {
        addStaticEffect(new IgnoreLegendRuleForControlledTokensEffect());
        addTrigger(Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        new MayPayManaEffect(
                                "{1}",
                                new CreateTokenCopyOfEnteringPermanentEffect(true, false, true),
                                "Pay {1} to create a hasty token copy of that legendary permanent?")));
    }
}
