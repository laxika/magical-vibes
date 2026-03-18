package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HowlpackAlpha;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "193")
public class MayorOfAvabruck extends Card {

    public MayorOfAvabruck() {
        // Set up back face
        HowlpackAlpha backFace = new HowlpackAlpha();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Other Human creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));

        // At the beginning of each upkeep, if no spells were cast last turn, transform Mayor of Avabruck.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new NoSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "HowlpackAlpha";
    }
}
