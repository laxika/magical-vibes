package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "250")
public class NathOfTheGiltLeaf extends Card {

    public NathOfTheGiltLeaf() {
        // At the beginning of your upkeep, you may have target opponent discard a card at random.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true),
                "Have target opponent discard a card at random?"
        ));

        // Whenever an opponent discards a card, you may create a 1/1 green Elf Warrior creature token.
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new MayEffect(
                new CreateTokenEffect(
                        "Elf Warrior", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()),
                "Create a 1/1 green Elf Warrior creature token?"
        ));
    }
}
