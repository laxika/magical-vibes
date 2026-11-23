package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "98")
@CardRegistration(set = "5ED", collectorNumber = "123")
@CardRegistration(set = "ICE", collectorNumber = "97")
@CardRegistration(set = "ME2", collectorNumber = "67")
public class SibilantSpirit extends Card {

    public SibilantSpirit() {
        // Flying is loaded from Scryfall metadata.
        // Whenever this creature attacks, defending player may draw a card.
        addTrigger(Triggers.attacks(), new MayEffect(new DrawCardEffect(1, Subjects.defendingPlayer()),
                "Draw a card?", null, MayChoicePlayer.DEFENDING_PLAYER));
    }
}
