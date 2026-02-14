package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;

public class MarchOfTheMachines extends Card {

    public MarchOfTheMachines() {
        super("March of the Machines", CardType.ENCHANTMENT, "{3}{U}", CardColor.BLUE);

        setCardText("Each noncreature artifact is an artifact creature with power and toughness each equal to its mana value.");
        addEffect(EffectSlot.STATIC, new AnimateNoncreatureArtifactsEffect());
    }
}
