package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;

import java.util.List;

public class Pariah extends Card {

    public Pariah() {
        super("Pariah", CardType.ENCHANTMENT, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nAll damage that would be dealt to you is dealt to enchanted creature instead.");
        setNeedsTarget(true);
        setStaticEffects(List.of(new RedirectPlayerDamageToEnchantedCreatureEffect()));
    }
}
