package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;


@CardRegistration(set = "AKH", collectorNumber = "147")
@CardRegistration(set = "AKR", collectorNumber = "172")
public class PursueGlory extends Card {

    public PursueGlory() {
        // Attacking creatures get +2/+0 until end of turn.
        addSpellEffect(BoostEffect.allCreatures(2, 0, new PermanentIsAttackingPredicate()));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
