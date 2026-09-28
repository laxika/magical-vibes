package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnThenDealManaValueDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MSC", collectorNumber = "685")
public class DaredevilFearlessFighter extends Card {

    public DaredevilFearlessFighter() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(opponent).addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE,
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER));

        addEffect(EffectSlot.ON_ATTACK,
                new ExileTopCardMayPlayThisTurnThenDealManaValueDamageToControllerEffect());
    }
}
