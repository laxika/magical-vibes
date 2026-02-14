package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

public class Reminisce extends Card {

    public Reminisce() {
        super("Reminisce", CardType.SORCERY, "{2}{U}", CardColor.BLUE);

        setCardText("Target player shuffles their graveyard into their library.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ShuffleGraveyardIntoLibraryEffect());
    }
}
