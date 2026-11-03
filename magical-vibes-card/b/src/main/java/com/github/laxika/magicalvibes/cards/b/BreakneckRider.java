package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NeckBreaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "147")
public class BreakneckRider extends Card {

    public BreakneckRider() {
        setBackFaceCard(new NeckBreaker());

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoSpellsCastLastTurn()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "NeckBreaker";
    }
}
