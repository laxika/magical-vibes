package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TerrorOfKruinPass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "ISD", collectorNumber = "152")
public class KruinOutlaw extends Card {

    public KruinOutlaw() {
        // Set up back face
        TerrorOfKruinPass backFace = new TerrorOfKruinPass();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // First strike is loaded from Scryfall.

        // At the beginning of each upkeep, if no spells were cast last turn, transform Kruin Outlaw.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new NoSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TerrorOfKruinPass";
    }
}
