package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;

public class AgonizingMemories extends Card {

    public AgonizingMemories() {
        super("Agonizing Memories", CardType.SORCERY, "{2}{B}{B}", CardColor.BLACK);

        setCardText("Look at target player's hand and choose two cards from it. Put them on top of that player's library in any order.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandToTopOfLibraryEffect(2));
    }
}
