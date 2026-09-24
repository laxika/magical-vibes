package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "196")
@CardRegistration(set = "CMM", collectorNumber = "525")
public class VindictiveLich extends Card {

    public VindictiveLich() {
        var opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");

        addEffect(EffectSlot.ON_DEATH, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent sacrifices a creature of their choice",
                        new SacrificePermanentsEffect(
                                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.TARGET_PLAYER),
                        opponent),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent discards two cards",
                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER),
                        opponent),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses 5 life",
                        new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER),
                        opponent)
        )));
    }
}
