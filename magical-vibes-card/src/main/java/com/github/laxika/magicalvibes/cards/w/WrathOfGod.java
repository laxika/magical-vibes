package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesEffect;

public class WrathOfGod extends Card {

    public WrathOfGod() {
        super("Wrath of God", CardType.SORCERY, "{2}{W}{W}", CardColor.WHITE);

        setCardText("Destroy all creatures. They can't be regenerated.");
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesEffect(true));
    }
}
