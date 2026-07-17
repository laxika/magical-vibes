package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;

@CardRegistration(set = "ALA", collectorNumber = "182")
public class PrinceOfThralls extends Card {

    public PrinceOfThralls() {
        // Whenever a permanent an opponent controls is put into a graveyard, put that card onto the
        // battlefield under your control unless that opponent pays 3 life.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new StealDyingOpponentPermanentUnlessPaysLifeEffect(3));
    }
}
