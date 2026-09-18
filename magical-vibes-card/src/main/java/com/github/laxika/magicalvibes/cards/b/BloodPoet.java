package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "34")
public class BloodPoet extends Card {

    public BloodPoet() {
        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "+1: Blood Poet gains lifelink until end of turn."
        ).withSpark());

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(SequenceEffect.of(
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        new GainLifeEffect(new LastDiscardedCardManaValue()))),
                "−3: Target opponent discards a card. You gain life equal to its mana value.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")
        ).withSpark());
    }
}
