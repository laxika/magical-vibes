package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "DSK", collectorNumber = "82")
public class VanishFromSight extends Card {

    public VanishFromSight() {
        addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0));
        addEffect(EffectSlot.SPELL, new SurveilEffect(1));
    }
}
