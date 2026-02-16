package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;

public class AgonizingMemories extends Card {

    public AgonizingMemories() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandToTopOfLibraryEffect(2));
    }
}
