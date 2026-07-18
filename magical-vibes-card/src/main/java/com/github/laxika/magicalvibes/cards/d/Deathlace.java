package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;

@CardRegistration(set = "4ED", collectorNumber = "131")
public class Deathlace extends Card {

    public Deathlace() {
        // Target spell or permanent becomes black (replaces its colors, indefinitely). Spell-or-permanent
        // targeting is inferred from the effect's PERMANENT spec + spell capability, like Purelace.
        addEffect(EffectSlot.SPELL, new SetTargetColorEffect(CardColor.BLACK));
    }
}
