package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForTargetCreatureNameToBattlefieldEffect;

@CardRegistration(set = "STH", collectorNumber = "37")
public class MaskOfTheMimic extends Card {

    public MaskOfTheMimic() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        addEffect(EffectSlot.SPELL, new SearchLibraryForTargetCreatureNameToBattlefieldEffect());
    }
}
