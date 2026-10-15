package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PIP", collectorNumber = "118")
@CardRegistration(set = "PIP", collectorNumber = "428")
@CardRegistration(set = "PIP", collectorNumber = "646")
@CardRegistration(set = "PIP", collectorNumber = "956")
public class SentinelSarahLyons extends Card {

    public SentinelSarahLyons() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 1),
                new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES)));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player")).addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(
                        new MinimumAttackers(3),
                        new DealDamageToPlayersEffect(
                                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER),
                                DamageRecipient.TARGET_PLAYER)));
    }
}
