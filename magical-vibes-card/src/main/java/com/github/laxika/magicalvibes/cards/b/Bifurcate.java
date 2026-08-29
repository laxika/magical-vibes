package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForTargetCreatureNameToBattlefieldEffect;

@CardRegistration(set = "MMQ", collectorNumber = "230")
public class Bifurcate extends Card {

    public Bifurcate() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForTargetCreatureNameToBattlefieldEffect(true));
    }
}
