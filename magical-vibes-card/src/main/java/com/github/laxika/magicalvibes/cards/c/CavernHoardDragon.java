package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPermanentCountAmongOpponents;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "HOC", collectorNumber = "23")
@CardRegistration(set = "HOC", collectorNumber = "63")
public class CavernHoardDragon extends Card {

    public CavernHoardDragon() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new GreatestPermanentCountAmongOpponents(new PermanentIsArtifactPredicate())));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                CreateTokenEffect.ofTreasureToken(new PermanentCount(
                        new PermanentIsArtifactPredicate(), CountScope.TARGET_PLAYER)));
    }
}
