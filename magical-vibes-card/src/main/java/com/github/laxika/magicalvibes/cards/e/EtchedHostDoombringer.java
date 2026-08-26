package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "102")
public class EtchedHostDoombringer extends Card {

    public EtchedHostDoombringer() {
        PermanentPredicate protectedByOpponent = new PermanentProtectedByOpponentOfSourceControllerPredicate();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses 2 life and you gain 2 life",
                        List.of(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target battle. If an opponent protects it, remove three defense counters from it. Otherwise, put three defense counters on it",
                        List.of(
                                new ConditionalEffect(new TargetPermanentMatches(protectedByOpponent),
                                        new RemoveCounterFromTargetPermanentEffect(CounterType.DEFENSE, null, 3)),
                                new ConditionalEffect(new NotCondition(new TargetPermanentMatches(protectedByOpponent)),
                                        new PutCounterOnTargetPermanentEffect(CounterType.DEFENSE, 3))),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsBattlePredicate(),
                                "Target must be a battle"))
        )));
    }
}
