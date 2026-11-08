package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class ErayosEssence extends Card {

    public ErayosEssence() {
        addTrigger(Triggers.opponentCastsSpell().nthThisTurn(1), new CounterSpellEffect());
    }
}
