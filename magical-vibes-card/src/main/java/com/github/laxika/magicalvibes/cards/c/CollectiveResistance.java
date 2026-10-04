package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDC", collectorNumber = "196")
public class CollectiveResistance extends Card {

    public CollectiveResistance() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{G}"));

        setAllowSharedTargets(true);
        var artifactFilter = new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact.");
        var enchantmentFilter = new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment.");
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        artifactFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        new DestroyTargetPermanentEffect(),
                        enchantmentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains hexproof and indestructible until end of turn",
                        new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.TARGET),
                        creatureFilter)
        )));
    }
}
