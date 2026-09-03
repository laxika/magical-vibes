package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "SNC", collectorNumber = "183")
public class EndlessDetour extends Card {

    public EndlessDetour() {
        addEffect(EffectSlot.SPELL, new PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect());
    }
}
