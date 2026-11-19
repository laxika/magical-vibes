package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "60")
public class Glademuse extends Card {

    public Glademuse() {
        addTrigger(Triggers.playerCastsSpell().notDuringCastersTurn(), new DrawCardEffect(1, Subjects.triggeringPlayer()));
    }
}
