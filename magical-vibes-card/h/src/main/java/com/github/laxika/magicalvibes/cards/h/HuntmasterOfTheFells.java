package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RavagerOfTheFells;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "140")
@CardRegistration(set = "SIS", collectorNumber = "64")
@CardRegistration(set = "INR", collectorNumber = "241")
@CardRegistration(set = "INR", collectorNumber = "325")
@CardRegistration(set = "INR", collectorNumber = "470")
@CardRegistration(set = "SLD", collectorNumber = "495")
@CardRegistration(set = "SLD", collectorNumber = "700")
@CardRegistration(set = "V17", collectorNumber = "11")
public class HuntmasterOfTheFells extends Card {

    public HuntmasterOfTheFells() {
        setBackFaceCard(new RavagerOfTheFells());

        addTrigger(Triggers.enters(ObjectRelation.THIS), wolfToken(), new GainLifeEffect(2));
        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS), wolfToken(), new GainLifeEffect(2));

        // At the beginning of each upkeep, if no spells were cast last turn, transform Huntmaster.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    private static CreateTokenEffect wolfToken() {
        return new CreateTokenEffect(
                "Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(), Set.of()
        );
    }

    @Override
    public String getBackFaceClassName() {
        return "RavagerOfTheFells";
    }
}
