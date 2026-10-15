package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandPermanentToBattlefieldEffect;

@CardRegistration(set = "C18", collectorNumber = "30")
public class GenesisStorm extends Card {

    public GenesisStorm() {
        // When you cast this spell, copy it for each time you've cast your commander from the
        // command zone this game.
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellForEachCommanderCastEffect(false));

        // Reveal cards from the top of your library until you reveal a nonland permanent card.
        addEffect(EffectSlot.SPELL, new RevealUntilNonlandPermanentToBattlefieldEffect());
    }
}
