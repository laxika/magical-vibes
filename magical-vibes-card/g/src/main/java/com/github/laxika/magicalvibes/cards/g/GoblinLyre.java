package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "319")
public class GoblinLyre extends Card {

    public GoblinLyre() {
        // The win branch counts the creatures you control; the loss branch counts the creatures the
        // targeted opponent (or the targeted planeswalker's controller) controls, which is what
        // CountScope.TARGET_PLAYER resolves the entry's target to.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new FlipCoinWinEffect(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                                PlayerRelation.OPPONENT),
                        new DealDamageToPlayersEffect(
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER),
                                DamageRecipient.CONTROLLER))),
                "Sacrifice Goblin Lyre: Flip a coin. If you win the flip, Goblin Lyre deals damage to "
                        + "target opponent or planeswalker equal to the number of creatures you control. "
                        + "If you lose the flip, Goblin Lyre deals damage to you equal to the number of "
                        + "creatures that opponent or that planeswalker's controller controls.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or planeswalker")));
    }
}
