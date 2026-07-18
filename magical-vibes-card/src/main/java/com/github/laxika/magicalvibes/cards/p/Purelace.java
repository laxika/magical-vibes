package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;

@CardRegistration(set = "4ED", collectorNumber = "43")
public class Purelace extends Card {

    public Purelace() {
        // Target spell or permanent becomes white (replaces its colors, indefinitely). Spell-or-permanent
        // targeting is inferred from the effect's PERMANENT spec + spell capability, like Glamerdye.
        addEffect(EffectSlot.SPELL, new SetTargetColorEffect(CardColor.WHITE));
    }
}
