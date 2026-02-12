package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;

import java.util.List;

public class SpiritLink extends Card {

    public SpiritLink() {
        super("Spirit Link", CardType.ENCHANTMENT, "{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nWhenever enchanted creature deals damage, you gain that much life.");
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GainLifeEqualToDamageDealtEffect());
    }
}
