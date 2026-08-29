package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

@CardRegistration(set = "BIG", collectorNumber = "11")
public class GenerousPlunderer extends Card {

    public GenerousPlunderer() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CreateTokenThenEffect(
                        CreateTokenEffect.ofTreasureToken(1),
                        new CreateTokenForTargetPlayerEffect(
                                CreateTokenEffect.ofTreasureToken(1).withTapped(true),
                                PlayerRelation.OPPONENT)),
                "Create a Treasure token?"));
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAttackedTargetEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.DEFENDING_PLAYER)));
    }
}
