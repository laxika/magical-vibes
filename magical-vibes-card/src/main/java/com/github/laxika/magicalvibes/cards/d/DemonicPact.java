package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "92")
@CardRegistration(set = "AKR", collectorNumber = "99")
public class DemonicPact extends Card {

    public DemonicPact() {
        // At the beginning of your upkeep, choose one that hasn't been chosen. The mode (and its
        // targets) are picked as the trigger goes on the stack; each chosen mode is consumed, so the
        // fourth upkeep forces whichever mode is left.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Demonic Pact deals 4 damage to any target and you gain 4 life",
                        List.of(new DealDamageToAnyTargetEffect(4), new GainLifeEffect(4))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent discards two cards",
                        List.of(new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw two cards",
                        new DrawCardEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "You lose the game",
                        new ControllerLosesGameEffect()))));
    }
}
