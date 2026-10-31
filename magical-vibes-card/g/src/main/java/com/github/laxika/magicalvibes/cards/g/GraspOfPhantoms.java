package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "58")
@CardRegistration(set = "MM3", collectorNumber = "41")
public class GraspOfPhantoms extends Card {

    public GraspOfPhantoms() {
        target(TargetFilters.creature()).addSpellEffect(new PutTargetOnTopOfLibraryEffect());
        addCastingOption(new FlashbackCast("{7}{U}"));
    }
}
