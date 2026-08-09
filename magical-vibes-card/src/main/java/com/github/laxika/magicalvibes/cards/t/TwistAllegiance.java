package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "BOK", collectorNumber = "120")
public class TwistAllegiance extends Card {

    public TwistAllegiance() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL,
                new ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect(creature, ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.CONTROLLED, creature))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                        TapUntapScope.TARGET_PLAYERS_PERMANENTS, creature))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES))
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET_PLAYERS_CREATURES));
    }
}
