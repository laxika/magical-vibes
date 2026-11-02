package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class PyreOfTheWorldTree extends Card {

    public PyreOfTheWorldTree() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new DealDamageToAnyTargetEffect(2)),
                "Discard a land card: This enchantment deals 2 damage to any target."));

        addTrigger(Triggers.youDiscardCard(), new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.LAND),
                new ExileTopCardMayPlayThisTurnEffect(false)));
    }
}
