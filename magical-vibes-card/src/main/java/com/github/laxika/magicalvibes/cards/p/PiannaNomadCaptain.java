package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ODY", collectorNumber = "39")
public class PiannaNomadCaptain extends Card {

    public PiannaNomadCaptain() {
        // Whenever Pianna attacks, attacking creatures get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK,
                new BoostAllOwnCreaturesEffect(1, 1, new PermanentIsAttackingPredicate()));
    }
}
