package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;

import java.util.List;

public class HeartOfLight extends Card {

    public HeartOfLight() {
        super("Heart of Light", CardType.ENCHANTMENT, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nPrevent all damage that would be dealt to and dealt by enchanted creature.");
        setNeedsTarget(true);
        setAura(true);
        setStaticEffects(List.of(new PreventAllDamageToAndByEnchantedCreatureEffect()));
    }
}
