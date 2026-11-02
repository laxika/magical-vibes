package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnThenDealManaValueDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "685")
public class DaredevilFearlessFighter extends Card {

    public DaredevilFearlessFighter() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(opponent).addTrigger(Triggers.youAreDealtDamageBySourceYouControl(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER));

        addTrigger(Triggers.attacks(),
                new ExileTopCardMayPlayThisTurnThenDealManaValueDamageToControllerEffect());
    }
}
