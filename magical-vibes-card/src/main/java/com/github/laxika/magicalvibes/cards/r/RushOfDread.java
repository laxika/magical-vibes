package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "104")
public class RushOfDread extends Card {

    public RushOfDread() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{2}", "{2}")));

        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var creatureFilter = new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent sacrifices half the creatures they control, rounded up",
                        new SacrificePermanentsEffect(
                                new HalvedRoundedUp(new PermanentCount(
                                        new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER)),
                                creatureFilter,
                                SacrificeRecipient.TARGET_PLAYER),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent discards half the cards in their hand, rounded up",
                        new DiscardEffect(
                                new HalvedRoundedUp(new CardsInHand(CountScope.TARGET_PLAYER)),
                                DiscardRecipient.TARGET_PLAYER),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses half their life, rounded up",
                        new LoseLifeEffect(
                                new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                                LoseLifeRecipient.TARGET_PLAYER),
                        opponentFilter)
        )));
    }
}
