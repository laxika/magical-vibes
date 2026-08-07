package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "44")
public class AnchorToTheAether extends Card {

    public AnchorToTheAether() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect())
                .addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
