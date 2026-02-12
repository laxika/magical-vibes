package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;

import java.util.List;

public class SerrasEmbrace extends Card {

    public SerrasEmbrace() {
        super("Serra's Embrace", CardType.ENCHANTMENT, "{2}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.AURA));
        setCardText("Enchant creature\nEnchanted creature gets +2/+2 and has flying and vigilance.");
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureEffect(2, 2));
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.FLYING));
        addEffect(EffectSlot.STATIC, new GrantKeywordToEnchantedCreatureEffect(Keyword.VIGILANCE));
    }
}
