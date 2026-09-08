package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithAnotherControlledPermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "219")
public class YennaRedtoothRegent extends Card {

    public YennaRedtoothRegent() {
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentNotPredicate(new PermanentSharesNameWithAnotherControlledPermanentPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                                false, false, null, Set.of(), true),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.AURA)),
                                SequenceEffect.of(
                                        new UntapPermanentsEffect(TapUntapScope.SELF),
                                        new ScryEffect(2)))
                ),
                "{2}, {T}: Create a token that's a copy of target enchantment you control that doesn't have the same name as another permanent you control, except it isn't legendary. If the token is an Aura, untap Yenna, then scry 2. Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        targetPredicate,
                        "Target must be an enchantment you control that doesn't have the same name as another permanent you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
