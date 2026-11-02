package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "251")
@CardRegistration(set = "CMD", collectorNumber = "196")
@CardRegistration(set = "OTC", collectorNumber = "221")
@CardRegistration(set = "C16", collectorNumber = "195")
public class EdricSpymasterOfTrest extends Card {

    public EdricSpymasterOfTrest() {
        MayEffect draw = new MayEffect(
                new DrawCardForTriggeringPermanentControllerEffect(),
                "Draw a card?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER);
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(), draw);
    }
}
