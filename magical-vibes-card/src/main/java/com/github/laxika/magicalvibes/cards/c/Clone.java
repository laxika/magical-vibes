package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;

import java.util.List;

public class Clone extends Card {

    public Clone() {
        super("Clone", CardType.CREATURE, "{3}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        setCardText("You may have Clone enter the battlefield as a copy of any creature on the battlefield.");
        setPower(0);
        setToughness(0);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyCreatureOnEnterEffect());
    }
}
