package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "266")
public class HarborGuardian extends Card {

    public HarborGuardian() {
        // Reach is loaded from Scryfall metadata.
        // Whenever this creature attacks, defending player may draw a card.
        addTrigger(Triggers.attacks(), new MayEffect(new DrawCardEffect(1, Subjects.defendingPlayer()),
                "Draw a card?", null, MayChoicePlayer.DEFENDING_PLAYER));
    }
}
