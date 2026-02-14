package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

public class MindBend extends Card {

    public MindBend() {
        super("Mind Bend", CardType.INSTANT, "{U}", CardColor.BLUE);

        setCardText("Change the text of target permanent by replacing all instances of one color word with another or one basic land type with another.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect());
    }
}
