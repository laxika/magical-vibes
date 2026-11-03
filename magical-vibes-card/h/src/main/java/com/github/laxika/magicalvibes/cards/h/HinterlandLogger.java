package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TimberShredder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "203")
@CardRegistration(set = "SOI", collectorNumber = "210")
@CardRegistration(set = "SIR", collectorNumber = "201")
public class HinterlandLogger extends Card {

    public HinterlandLogger() {
        setBackFaceCard(new TimberShredder());

        // At the beginning of each upkeep, if no spells were cast last turn, transform this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoSpellsCastLastTurn()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "TimberShredder";
    }
}
