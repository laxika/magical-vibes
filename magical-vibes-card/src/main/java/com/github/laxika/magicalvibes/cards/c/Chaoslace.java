package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;

@CardRegistration(set = "4ED", collectorNumber = "182")
public class Chaoslace extends Card {

    public Chaoslace() {
        // Target spell or permanent becomes red (replaces its colors, indefinitely). Spell-or-permanent
        // targeting is inferred from the effect's PERMANENT spec + spell capability, like Deathlace.
        addEffect(EffectSlot.SPELL, new SetTargetColorEffect(CardColor.RED));
    }
}
