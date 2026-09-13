package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "184")
public class FavorOfJukai extends Card {

    public FavorOfJukai() {
        var creature = new PermanentIsCreaturePredicate();
        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                creature));

        target(new PermanentPredicateTargetFilter(artifactOrCreature,
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        creature,
                        new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE),
                        null))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        creature,
                        new GrantKeywordEffect(Keyword.REACH, GrantScope.ENCHANTED_CREATURE),
                        null));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new BoostTargetCreatureEffect(3, 3),
                        new GrantKeywordEffect(Keyword.REACH, GrantScope.TARGET)),
                "Channel — {1}{G}, Discard this card: Target creature gets +3/+3 and gains reach until end of turn.",
                TargetFilters.creature()
        ));
    }
}
