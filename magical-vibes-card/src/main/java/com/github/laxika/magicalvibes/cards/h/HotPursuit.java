package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
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

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "32")
@CardRegistration(set = "MKC", collectorNumber = "342")
public class HotPursuit extends Card {

    public HotPursuit() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SuspectEffect(GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GoadTargetCreatureUntilSourceLeavesEffect());

        PermanentPredicate goadedOrSuspectedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsGoadedPredicate(),
                        new PermanentIsSuspectedPredicate()))));
        AtLeastPlayersLostGame condition = new AtLeastPlayersLostGame(2);
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(condition,
                        new GainControlOfAllPermanentsMatchingEffect(
                                goadedOrSuspectedCreature,
                                ControlDuration.END_OF_TURN)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(condition,
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, goadedOrSuspectedCreature)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(condition,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES,
                                goadedOrSuspectedCreature)));
    }
}
