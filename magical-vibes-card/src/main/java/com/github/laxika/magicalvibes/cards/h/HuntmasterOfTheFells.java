package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RavagerOfTheFells;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "140")
public class HuntmasterOfTheFells extends Card {

    public HuntmasterOfTheFells() {
        RavagerOfTheFells backFace = new RavagerOfTheFells();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        addWolfAndLifeTrigger(EffectSlot.ON_ENTER_BATTLEFIELD);
        addWolfAndLifeTrigger(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE);

        // At the beginning of each upkeep, if no spells were cast last turn, transform Huntmaster.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new NoSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }

    private void addWolfAndLifeTrigger(EffectSlot slot) {
        addEffect(slot, new CreateTokenEffect(
                "Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(), Set.of()
        ));
        addEffect(slot, new GainLifeEffect(2));
    }

    @Override
    public String getBackFaceClassName() {
        return "RavagerOfTheFells";
    }
}
