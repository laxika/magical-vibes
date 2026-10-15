package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "9ED", collectorNumber = "107")
@CardRegistration(set = "POR", collectorNumber = "75")
@CardRegistration(set = "P02", collectorNumber = "57")
@CardRegistration(set = "M14", collectorNumber = "74")
@CardRegistration(set = "TMP", collectorNumber = "96")
@CardRegistration(set = "TPR", collectorNumber = "73")
@CardRegistration(set = "S99", collectorNumber = "55")
@CardRegistration(set = "S00", collectorNumber = "19")
public class TimeEbb extends Card {

    public TimeEbb() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
