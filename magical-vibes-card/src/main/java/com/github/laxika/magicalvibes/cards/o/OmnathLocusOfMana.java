package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.UnspentMana;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;

@CardRegistration(set = "WWK", collectorNumber = "109")
public class OmnathLocusOfMana extends Card {

    public OmnathLocusOfMana() {
        addEffect(EffectSlot.STATIC, new PreventManaDrainEffect(ManaColor.GREEN));
        UnspentMana greenMana = new UnspentMana(ManaColor.GREEN);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(greenMana, greenMana));
    }
}
