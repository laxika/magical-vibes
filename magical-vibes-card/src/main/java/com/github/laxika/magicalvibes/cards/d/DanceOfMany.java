package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "78")
public class DanceOfMany extends Card {

    public DanceOfMany() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                )),
                "Target must be a nontoken creature"
        ))
                // "When this enchantment enters, create a token that's a copy of target nontoken creature."
                // The token and this enchantment become linked (see the leaves-battlefield triggers below).
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenCopyAndLinkToSourceEffect())
                // "When this enchantment leaves the battlefield, exile the token."
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.EXILE))
                // "At the beginning of your upkeep, sacrifice this enchantment unless you pay {U}{U}."
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new ForcedCostOrElseEffect(
                                new PayManaCost("{U}{U}"),
                                List.of(new SacrificeSelfEffect()),
                                true));
        // "When the token leaves the battlefield, sacrifice this enchantment." — this trigger lives on
        // the created token (attached in CreateTokenCopyAndLinkToSourceEffectHandler), since the token is
        // a copy of an arbitrary creature built at resolution time.
    }
}
