package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C16", collectorNumber = "37")
public class LudevicNecroAlchemist extends Card {

    public LudevicNecroAlchemist() {
        // At the beginning of each player's end step, that player may draw a card if an opponent
        // lost life this turn.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new MayEffect(new DrawCardEffect(1, Subjects.triggeringPlayer()), "Draw a card?", null,
                        MayChoicePlayer.ACTIVE_PLAYER)));
    }
}
