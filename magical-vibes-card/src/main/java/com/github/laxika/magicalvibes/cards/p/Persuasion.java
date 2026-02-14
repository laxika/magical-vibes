package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;

import java.util.List;

public class Persuasion extends Card {

    public Persuasion() {
        super("Persuasion", CardType.ENCHANTMENT, "{3}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nYou control enchanted creature.");
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
