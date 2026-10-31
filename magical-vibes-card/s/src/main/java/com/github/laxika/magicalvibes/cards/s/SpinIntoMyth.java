package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FatesealEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "60")
@CardRegistration(set = "ARC", collectorNumber = "8")
public class SpinIntoMyth extends Card {

    public SpinIntoMyth() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutTargetOnTopOfLibraryEffect())
                .addSpellEffect(new FatesealEffect(2));
    }
}
