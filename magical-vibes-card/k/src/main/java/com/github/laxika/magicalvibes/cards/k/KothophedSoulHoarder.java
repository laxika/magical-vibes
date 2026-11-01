package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "104")
@CardRegistration(set = "SLD", collectorNumber = "162")
@CardRegistration(set = "DMC", collectorNumber = "114")
public class KothophedSoulHoarder extends Card {

    public KothophedSoulHoarder() {
        // Whenever a permanent owned by another player is put into a graveyard from the battlefield,
        // you draw a card and you lose 1 life.
        addTrigger(Triggers.dies(null, ObjectRelation.OPPONENT_OWNS),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
