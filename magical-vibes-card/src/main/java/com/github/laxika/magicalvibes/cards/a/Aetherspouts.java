package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutAttackingCreaturesOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "M15", collectorNumber = "44")
public class Aetherspouts extends Card {

    public Aetherspouts() {
        addEffect(EffectSlot.SPELL, new PutAttackingCreaturesOnTopOrBottomOfLibraryEffect());
    }
}
