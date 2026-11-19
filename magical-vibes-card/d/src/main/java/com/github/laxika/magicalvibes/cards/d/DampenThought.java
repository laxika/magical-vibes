package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CHK", collectorNumber = "57")
@CardRegistration(set = "MMA", collectorNumber = "39")
public class DampenThought extends Card {

    public DampenThought() {
        // Target player mills four cards.
        addSpellEffect(new MillEffect(4, Subjects.targetPlayer()));

        // Splice onto Arcane {1}{U}
        addStaticEffect(new SpliceEffect(CardSubtype.ARCANE, "{1}{U}"));
    }
}
