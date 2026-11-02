package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "13")
@CardRegistration(set = "DBL", collectorNumber = "13")
public class ChaplainOfAlms extends Card {

    public ChaplainOfAlms() {
        setBackFaceCard(new ChapelShieldgeist());

        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(1));

        addCastingOption(new DisturbCast("{3}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChapelShieldgeist";
    }
}
