package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RampagingWerewolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "ISD", collectorNumber = "165")
public class TormentedPariah extends Card {

    public TormentedPariah() {
        // Set up back face
        RampagingWerewolf backFace = new RampagingWerewolf();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of each upkeep, if no spells were cast last turn, transform Tormented Pariah.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new NoSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "RampagingWerewolf";
    }
}
