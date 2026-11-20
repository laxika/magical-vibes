package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;

@CardRegistration(set = "AVR", collectorNumber = "43")
@CardRegistration(set = "IMA", collectorNumber = "41")
public class AmassTheComponents extends Card {

    public AmassTheComponents() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(1, HandToLibraryPlacement.BOTTOM));
    }
}
