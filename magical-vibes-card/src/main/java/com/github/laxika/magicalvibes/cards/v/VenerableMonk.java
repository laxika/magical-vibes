package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

public class VenerableMonk extends Card {

    public VenerableMonk() {
        super("Venerable Monk", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.MONK, CardSubtype.CLERIC));
        setCardText("When Venerable Monk enters the battlefield, you gain 2 life.");
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
    }
}
