package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamageFromSpellsEffect;

@CardRegistration(set = "TSP", collectorNumber = "34")
public class PlatedPegasus extends Card {

    public PlatedPegasus() {
        addEffect(EffectSlot.STATIC, new PreventFixedDamageFromSpellsEffect(1));
    }
}
