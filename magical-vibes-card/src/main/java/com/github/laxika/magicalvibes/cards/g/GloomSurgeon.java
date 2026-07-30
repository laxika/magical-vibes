package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToSelfAndExileFromLibraryEffect;

@CardRegistration(set = "AVR", collectorNumber = "104")
public class GloomSurgeon extends Card {

    public GloomSurgeon() {
        addEffect(EffectSlot.STATIC, new PreventCombatDamageToSelfAndExileFromLibraryEffect());
    }
}
