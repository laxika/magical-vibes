package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "97")
@CardRegistration(set = "FIC", collectorNumber = "190")
@CardRegistration(set = "FIC", collectorNumber = "477")
public class WakkaDevotedGuardian extends Card {

    public WakkaDevotedGuardian() {
        // Whenever Wakka deals combat damage to a player, destroy up to one target artifact that
        // player controls and put a +1/+1 counter on Wakka.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsArtifactPredicate(), 0),
                        "Destroy target artifact that player controls?"));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));

        // At the beginning of your end step, if a counter was put on Wakka this turn, put a
        // +1/+1 counter on each other creature you control.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new PutCounterOnSourceThisTurn(),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )))));
    }
}
