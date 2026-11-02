package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C18", collectorNumber = "30")
public class GenesisStorm extends Card {

    public GenesisStorm() {
        // When you cast this spell, copy it for each time you've cast your commander from the
        // command zone this game.
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellForEachCommanderCastEffect(false));

        // Reveal cards from the top of your library until you reveal a nonland permanent card.
        addSpellEffect(new RevealUntilNonlandPermanentToBattlefieldEffect());
    }
}
