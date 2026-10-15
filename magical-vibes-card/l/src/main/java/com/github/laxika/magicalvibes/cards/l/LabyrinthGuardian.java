package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;


@CardRegistration(set = "AKH", collectorNumber = "60")
@CardRegistration(set = "AKR", collectorNumber = "67")
public class LabyrinthGuardian extends Card {

    public LabyrinthGuardian() {
        // When this creature becomes the target of a spell, sacrifice it.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new SacrificeSelfEffect());

        // Embalm {3}{U} ({3}{U}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Illusion Warrior with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{3}{U}", "Illusion Warrior");
    }
}
