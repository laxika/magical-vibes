package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromSubtypeEffect;

public class TranceKujaFateDefied extends Card {

    public TranceKujaFateDefied() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromSubtypeEffect(CardSubtype.WIZARD));
    }
}
