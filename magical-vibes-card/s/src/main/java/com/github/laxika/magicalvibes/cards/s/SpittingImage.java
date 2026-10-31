package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EVE", collectorNumber = "162")
@CardRegistration(set = "C21", collectorNumber = "229")
@CardRegistration(set = "C16", collectorNumber = "236")
@CardRegistration(set = "CM2", collectorNumber = "166")
public class SpittingImage extends Card {

    public SpittingImage() {
        target(TargetFilters.creature())
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect());
        addCastingOption(new Retrace());
    }
}
