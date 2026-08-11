package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "M20", collectorNumber = "42")
public class AetherGust extends Card {

    public AetherGust() {
        addEffect(EffectSlot.SPELL, new PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect());
    }
}
