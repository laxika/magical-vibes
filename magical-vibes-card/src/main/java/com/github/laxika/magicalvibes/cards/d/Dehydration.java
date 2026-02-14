package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;

import java.util.List;

public class Dehydration extends Card {

    public Dehydration() {
        super("Dehydration", CardType.ENCHANTMENT, "{3}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nEnchanted creature doesn't untap during its controller's untap step.");
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new EnchantedCreatureDoesntUntapEffect());
    }
}
