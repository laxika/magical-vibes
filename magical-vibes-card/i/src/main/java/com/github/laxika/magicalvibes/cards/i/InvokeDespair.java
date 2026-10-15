package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "101")
public class InvokeDespair extends Card {

    public InvokeDespair() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        target(opponent)
                .addEffect(EffectSlot.SPELL,
                        sacrificeOrElse(new PermanentIsCreaturePredicate(), "a creature"))
                .addEffect(EffectSlot.SPELL,
                        sacrificeOrElse(new PermanentIsEnchantmentPredicate(), "an enchantment"))
                .addEffect(EffectSlot.SPELL,
                        sacrificeOrElse(new PermanentIsPlaneswalkerPredicate(), "a planeswalker"));
    }

    private ForcedCostOrElseEffect sacrificeOrElse(PermanentPredicate filter, String description) {
        return new ForcedCostOrElseEffect(
                new SacrificePermanentCost(filter, "Sacrifice " + description),
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                        new DrawCardEffect(1)),
                false, false, true, false, List.of());
    }
}
