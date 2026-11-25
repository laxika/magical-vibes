package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "57")
public class FrightfulDelusion extends Card {

    public FrightfulDelusion() {
        // Counter target spell unless its controller pays {1}. That player discards a card: the next
        // instruction (CR 608.2c), "that player" being the spell's controller as it was countered (CR 608.2h).
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(1));
        addSpellEffect(new DiscardEffect(1, Subjects.controllerOf(Subjects.targetSpell())));
    }
}
