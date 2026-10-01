package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "593")
public class NorthPoleResearchBase extends Card {

    public NorthPoleResearchBase() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                        new DrawCardForTargetPlayerEffect(1, false, true),
                        new CreateTokenForTargetPlayerEffect(
                                CreateTokenEffect.ofTreasureToken(1), PlayerRelation.OPPONENT)));

        PermanentPredicate nontokenCreatureOpponent = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        CreateTokenEffect alien = new CreateTokenEffect(
                1, "Alien", 2, 2, CardColor.WHITE, List.of(CardSubtype.ALIEN), Set.of(), Set.of());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new CreateTokenThenEffect(
                alien,
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET, nontokenCreatureOpponent),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN))));
    }
}
