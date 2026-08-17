package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "151")
public class PretendersClaim extends Card {

    public PretendersClaim() {
        // Enchant creature.
        target(TargetFilters.creature());

        // Whenever enchanted creature becomes blocked, tap all lands defending player controls.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new TapPermanentsEffect(
                TapUntapScope.ALL_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentControlledByDefendingPlayerPredicate()))));
    }
}
