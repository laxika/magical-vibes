package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;

public class TempestOfLight extends Card {

    public TempestOfLight() {
        super("Tempest of Light", CardType.INSTANT, "{2}{W}", CardColor.WHITE);

        setCardText("Destroy all enchantments.");
        addEffect(EffectSlot.SPELL, new DestroyAllEnchantmentsEffect());
    }
}
