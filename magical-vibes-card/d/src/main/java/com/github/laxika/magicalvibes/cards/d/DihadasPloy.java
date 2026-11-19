package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MH2", collectorNumber = "193")
public class DihadasPloy extends Card {

    public DihadasPloy() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
        addSpellEffect(new GainLifeEffect(new CardsDiscardedOrCycledThisTurn()));
        addCastingOption(new JumpStartCast());
    }
}
