package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;

public class GloriousAnthem extends Card {

    public GloriousAnthem() {
        super("Glorious Anthem", CardType.ENCHANTMENT, "{1}{W}{W}", CardColor.WHITE);

        setCardText("Creatures you control get +1/+1.");
        addEffect(EffectSlot.STATIC, new BoostOwnCreaturesEffect(1, 1));
    }
}
