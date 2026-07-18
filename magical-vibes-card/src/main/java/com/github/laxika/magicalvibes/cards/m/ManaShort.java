package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesAllUnspentManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "7ED", collectorNumber = "86")
@CardRegistration(set = "6ED", collectorNumber = "80")
@CardRegistration(set = "4ED", collectorNumber = "85")
public class ManaShort extends Card {

    public ManaShort() {
        // Tap all lands target player controls and that player loses all unspent mana.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new TapPermanentsEffect(
                        TapUntapScope.TARGET_PLAYERS_PERMANENTS,
                        new PermanentIsLandPredicate()))
                .addEffect(EffectSlot.SPELL, new TargetPlayerLosesAllUnspentManaEffect());
    }
}
