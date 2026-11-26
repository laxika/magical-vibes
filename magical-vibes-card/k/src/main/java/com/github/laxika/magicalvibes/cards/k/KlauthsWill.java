package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "51")
public class KlauthsWill extends Card {

    public KlauthsWill() {
        PermanentPredicate withoutFlying = new PermanentNotPredicate(
                new PermanentHasKeywordPredicate(Keyword.FLYING));
        PermanentPredicateTargetFilter artifactOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                "Targets must be artifacts and/or enchantments");

        addSpellEffect(ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Klauth's Will deals X damage to each creature without flying",
                        DealDamageEffect.toEachCreature(new XValue(), withoutFlying)),
                ChooseOneEffect.ChooseOneOption.upToXTargets(
                        "Destroy up to X target artifacts and/or enchantments",
                        DestroyTargetPermanentEffect.eachTarget(), artifactOrEnchantment, Integer.MAX_VALUE)
        ), new ControlledCommanderAsCast()));
    }
}
