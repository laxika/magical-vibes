package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "YBRO", collectorNumber = "17")
public class AssembleTheTeam extends Card {

    public AssembleTheTeam() {
        addEffect(EffectSlot.SPELL, SearchLibraryEffect.topThirdOfLibraryToHand());
    }
}
