package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "230")
public class StatusStatue extends Card {

    public StatusStatue() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        TargetFilter artifactCreatureOrEnchantment = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact, creature, or enchantment"
        );
        CardEffect status = SequenceEffect.of(
                new BoostTargetCreatureEffect(1, 1),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET));
        CardEffect statue = new DestroyTargetPermanentEffect();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Status — Target creature gets +1/+1 and gains deathtouch until end of turn",
                        status,
                        creature
                ).withManaCost("{B/G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Statue — Destroy target artifact, creature, or enchantment",
                        statue,
                        artifactCreatureOrEnchantment
                ).withManaCost("{2}{B}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Status and then Statue",
                        List.of(status, statue),
                        List.of(creature, artifactCreatureOrEnchantment)
                ).withManaCost("{2}{B}{G}{B/G}")
        )));
    }
}
