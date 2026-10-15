package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "138")
public class UrbanBurgeoning extends Card {

    public UrbanBurgeoning() {
        // Enchant land. Enchanted land has "Untap this land during each other player's untap step."
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC,
                new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                        TurnStep.UNTAP, null, TapUntapScope.ENCHANTED));
    }
}
