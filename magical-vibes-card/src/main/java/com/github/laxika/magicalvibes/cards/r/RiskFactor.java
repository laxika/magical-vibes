package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "113")
public class RiskFactor extends Card {

    public RiskFactor() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        )).addEffect(EffectSlot.SPELL, new TargetPlayerChoosesOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Have Risk Factor deal 4 damage to you",
                        new DealDamageToPlayersEffect(4, DamageRecipient.TARGET_PLAYER)),
                new ChooseOneEffect.ChooseOneOption("Draw three cards", new DrawCardEffect(3))
        )));
        addCastingOption(new JumpStartCast());
    }
}
