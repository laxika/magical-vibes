package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentCreaturesAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "77")
public class WarsToll extends Card {

    public WarsToll() {
        // Whenever an opponent taps a land for mana, tap all lands that player controls.
        addTrigger(Triggers.opponentTapsLandForMana(), new TapPermanentsEffect(TapUntapScope.ALL_PERMANENTS,
                new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(),
                        new PermanentControlledByTriggeringPlayerPredicate()))));

        // If a creature an opponent controls attacks, all creatures that opponent controls attack if able.
        addStaticEffect(new OpponentCreaturesAttackTogetherEffect());
    }
}
