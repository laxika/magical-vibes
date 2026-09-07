package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "42")
public class BurdenOfProof extends Card {

    public BurdenOfProof() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE),
                                new PermanentControlledBySourceControllerPredicate())),
                        new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE),
                        new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE)))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE),
                                new PermanentControlledBySourceControllerPredicate())),
                        null,
                        new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE)),
                                "non-Detectives")));
    }
}
