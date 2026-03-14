package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect;

@CardRegistration(set = "M10", collectorNumber = "122")
public class XathridDemon extends Card {

    public XathridDemon() {
        // Flying, trample are auto-loaded from Scryfall

        // At the beginning of your upkeep, sacrifice a creature other than Xathrid Demon,
        // then each opponent loses life equal to the sacrificed creature's power.
        // If you can't, tap Xathrid Demon and you lose 7 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect(7));
    }
}
