package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "270")
public class SeersVision extends Card {

    public SeersVision() {
        addEffect(EffectSlot.STATIC, new RevealOpponentHandsEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        ChooseCardsFromTargetHandEffect.lookAtTargetHand(
                                new Fixed(1), HandChoiceDestination.DISCARD)),
                "Sacrifice this enchantment: Look at target player's hand and choose a card from it. "
                        + "That player discards that card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                null, null, ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
