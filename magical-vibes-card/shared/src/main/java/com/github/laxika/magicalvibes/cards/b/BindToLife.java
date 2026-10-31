package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCreatureOntoBattlefieldEffect;

/** Bind to Life, the prepare spell of Vastlands Scavenger // Bind to Life (SOS 166). */
public class BindToLife extends Card {

    public BindToLife() {
        addSpellEffect(new MillControllerAndPutMilledCreatureOntoBattlefieldEffect(7));
    }
}
