package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "125")
public class GreatTrainHeist extends Card {

    public GreatTrainHeist() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{2}{R}", "{2}", "{R}")));

        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Untap all creatures you control. If it's your combat phase, there is an additional combat phase after this phase.",
                        List.of(
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                        new PermanentIsCreaturePredicate()),
                                new ConditionalEffect(
                                        new AllConditions(List.of(new DuringCombat(), new ControllerTurn())),
                                        new AdditionalCombatPhaseEffect(1)))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+0 and gain first strike until end of turn.",
                        List.of(
                                new BoostAllOwnCreaturesEffect(1, 0),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES))),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target opponent. Whenever a creature you control deals combat damage to that player this turn, create a tapped Treasure token.",
                        new RegisterDelayedCombatDamageTokenEffect(CreateTokenEffect.ofTappedTreasureToken(1)),
                        opponentFilter)
        )));
    }
}
