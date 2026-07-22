package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TimberShredder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "INR", collectorNumber = "203")
public class HinterlandLogger extends Card {

    public HinterlandLogger() {
        TimberShredder backFace = new TimberShredder();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of each upkeep, if no spells were cast last turn, transform this creature.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TimberShredder";
    }
}
