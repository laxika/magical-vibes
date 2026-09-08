package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "127")
public class FinaleOfPromise extends Card {

    public FinaleOfPromise() {
        CardPredicate instant = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardMaxManaValueXPredicate()));
        CardPredicate sorcery = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.SORCERY),
                new CardMaxManaValueXPredicate()));

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_INSTANT_AND_ONE_SORCERY);
        setAllowSharedTargets(true);
        target(new GraveyardCardPredicateTargetFilter(instant, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .addEffect(EffectSlot.SPELL, castWithCopies(CardType.INSTANT));
        target(new GraveyardCardPredicateTargetFilter(sorcery, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .addEffect(EffectSlot.SPELL, castWithCopies(CardType.SORCERY));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }

    private ConditionalReplacementEffect castWithCopies(CardType type) {
        return new ConditionalReplacementEffect(
                new SpellXAtLeast(10),
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(type), new CardMaxManaValueXPredicate()))),
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(type), new CardMaxManaValueXPredicate())), 2));
    }
}
