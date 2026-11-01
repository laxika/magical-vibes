package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "4")
@CardRegistration(set = "MKM", collectorNumber = "336")
@CardRegistration(set = "MKM", collectorNumber = "377")
public class AureliasVindicator extends Card {

    public AureliasVindicator() {
        addMorph("{X}{3}{W}");
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(0, 2));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new ExileTargetCreaturesUntilSourceLeavesEffect(Integer.MAX_VALUE, true, true));
    }
}
