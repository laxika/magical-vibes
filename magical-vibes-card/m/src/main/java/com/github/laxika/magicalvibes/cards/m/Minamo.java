package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "24")
@CardRegistration(set = "OPCA", collectorNumber = "50")
public class Minamo extends Card {

    public Minamo() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new MayEffect(new DrawCardEffect(1, Subjects.triggeringPlayer()),
                        "Draw a card?", null, MayChoicePlayer.TRIGGERING_SPELL_CONTROLLER));
        addTrigger(Triggers.chaosEnsues(),
                new EachPlayerMayReturnCardFromGraveyardToHandEffect(new CardColorPredicate(CardColor.BLUE)));
    }
}
