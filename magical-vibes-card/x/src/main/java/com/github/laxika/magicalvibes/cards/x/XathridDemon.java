package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "122")
@CardRegistration(set = "C14", collectorNumber = "170")
public class XathridDemon extends Card {

    public XathridDemon() {
        // Flying, trample are auto-loaded from Scryfall

        // At the beginning of your upkeep, sacrifice a creature other than Xathrid Demon,
        // then each opponent loses life equal to the sacrificed creature's power.
        // If you can't, tap Xathrid Demon and you lose 7 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect(7));
    }
}
