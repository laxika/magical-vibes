package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BounceOwnCreatureOnUpkeepEffect;

public class SunkenHope extends Card {

    public SunkenHope() {
        super("Sunken Hope", CardType.ENCHANTMENT, "{3}{U}{U}", CardColor.BLUE);

        setCardText("At the beginning of each player's upkeep, that player returns a creature they control to its owner's hand.");
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new BounceOwnCreatureOnUpkeepEffect());
    }
}
