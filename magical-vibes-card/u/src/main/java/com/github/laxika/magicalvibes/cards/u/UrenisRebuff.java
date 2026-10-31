package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "63")
public class UrenisRebuff extends Card {

    public UrenisRebuff() {
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());
        addCastingOption(new HarmonizeCast("{5}{U}"));
    }
}
