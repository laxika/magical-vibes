package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.AtLeastPlayersLostGame;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "32")
@CardRegistration(set = "MKC", collectorNumber = "342")
public class HotPursuit extends Card {

    public HotPursuit() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new SuspectEffect(GrantScope.TARGET),
                        new GoadTargetCreatureUntilSourceLeavesEffect());

        PermanentPredicate goadedOrSuspectedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsGoadedPredicate(),
                        new PermanentIsSuspectedPredicate()))));
        AtLeastPlayersLostGame condition = new AtLeastPlayersLostGame(2);
        // "Untap them. They gain haste" refers to the creatures gained this way, which are no longer
        // goaded once you control them.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(condition),
                new GainControlOfAllPermanentsMatchingEffect(
                        goadedOrSuspectedCreature,
                        ControlDuration.END_OF_TURN,
                        List.of(
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))));
    }
}
