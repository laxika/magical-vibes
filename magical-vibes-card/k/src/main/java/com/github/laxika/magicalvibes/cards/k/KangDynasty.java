package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "35")
public class KangDynasty extends Card {

    public KangDynasty() {
        for (int chapter : List.of(1, 2)) {
            addTrigger(Triggers.chapter(chapter),
                    new TapPermanentsEffect(TapUntapScope.TARGET),
                    new GoadTargetCreatureUntilNextTurnEffect(),
                    new RegisterDelayedWatchedCreaturesCombatDamageEffect(
                            List.of(new DrawCardEffect()), true));
            setSagaChapterTargetGroups(chapter, List.of(
                    new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 99)));
        }
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addTrigger(Triggers.chapter(3),
                new BoostTargetCreatureEffect(cardsInHand, cardsInHand),
                new MakeCreatureUnblockableEffect());
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));
    }
}
