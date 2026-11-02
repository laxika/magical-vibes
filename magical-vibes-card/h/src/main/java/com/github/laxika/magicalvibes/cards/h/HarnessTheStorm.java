package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastSameNameCardFromGraveyardOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "163")
public class HarnessTheStorm extends Card {

    public HarnessTheStorm() {
        addTrigger(Triggers.youCastSpell(),
                new CastSameNameCardFromGraveyardOnSpellCastEffect());
    }
}
