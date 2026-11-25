package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ORI", collectorNumber = "92")
@CardRegistration(set = "AKR", collectorNumber = "99")
public class DemonicPact extends Card {

    public DemonicPact() {
        // At the beginning of your upkeep, choose one that hasn't been chosen. The mode (and its
        // targets) are picked as the ability goes on the stack; each chosen mode is used up, so the
        // fourth upkeep forces whichever mode is left.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), ChooseOneEffect.notYetChosen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Demonic Pact deals 4 damage to any target and you gain 4 life",
                        List.of(DealDamageEffect.toAnyTarget(4), new GainLifeEffect(4))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent discards two cards",
                        List.of(new DiscardEffect(2, Subjects.targetPlayer())),
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
