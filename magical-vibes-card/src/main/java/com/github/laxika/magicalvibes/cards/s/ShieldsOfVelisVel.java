package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "LRW", collectorNumber = "39")
public class ShieldsOfVelisVel extends Card {

    public ShieldsOfVelisVel() {
        // Creatures target player controls get +0/+1 and gain all creature types
        // (Changeling) until end of turn.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(0, 1, EachPermanentScope.TARGET_PLAYER))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET_PLAYERS_CREATURES));
    }
}
