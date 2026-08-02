package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "CHK", collectorNumber = "57")
public class DampenThought extends Card {

    public DampenThought() {
        // Target player mills four cards.
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_PLAYER));

        // Splice onto Arcane {1}{U}
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{1}{U}"));
    }
}
