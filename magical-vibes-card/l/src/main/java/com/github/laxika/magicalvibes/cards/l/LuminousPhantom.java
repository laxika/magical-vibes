package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Luminous Phantom — back face of Lunarch Veteran.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class LuminousPhantom extends Card {

    public LuminousPhantom() {
        // Whenever another creature you control leaves the battlefield, you gain 1 life.
        addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new GainLifeEffect(1));

        // If Luminous Phantom would be put into a graveyard from anywhere, exile it instead.
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
