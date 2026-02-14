package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;

import java.util.List;

public class RobeOfMirrors extends Card {

    public RobeOfMirrors() {
        super("Robe of Mirrors", CardType.ENCHANTMENT, "{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nEnchanted creature has shroud.");
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.SHROUD));
    }
}
