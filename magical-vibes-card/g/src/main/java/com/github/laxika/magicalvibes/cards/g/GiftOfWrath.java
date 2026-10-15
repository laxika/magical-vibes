package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "143")
public class GiftOfWrath extends Card {

    public GiftOfWrath() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        target(new PermanentPredicateTargetFilter(
                artifactOrCreature,
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new StaticBoostEffect(2, 2, Set.of(Keyword.MENACE), GrantScope.ENCHANTED_CREATURE),
                        null));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new CreateTokenEffect(
                "Spirit", 2, 2, CardColor.RED, List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.MENACE), Set.of()));
    }
}
