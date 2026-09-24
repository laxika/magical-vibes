package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LockMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "188")
@CardRegistration(set = "MSC", collectorNumber = "420")
public class TauntFromTheRampart extends Card {

    public TauntFromTheRampart() {
        PermanentAllOfPredicate opponentCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addEffect(EffectSlot.SPELL, new GoadCreaturesUntilNextTurnEffect(opponentCreatures));
        addEffect(EffectSlot.SPELL,
                new LockMatchingPermanentsEffect(opponentCreatures, false, true, false,
                        EffectDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
