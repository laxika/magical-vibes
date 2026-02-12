package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;

import java.util.List;

public class Pacifism extends Card {

    public Pacifism() {
        super("Pacifism", CardType.ENCHANTMENT, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nEnchanted creature can't attack or block.");
        setNeedsTarget(true);
        setStaticEffects(List.of(new EnchantedCreatureCantAttackOrBlockEffect()));
    }
}
