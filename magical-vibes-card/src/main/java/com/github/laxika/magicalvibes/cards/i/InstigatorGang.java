package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WildbloodPack;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ISD", collectorNumber = "149")
public class InstigatorGang extends Card {

    public InstigatorGang() {
        // Set up back face
        WildbloodPack backFace = new WildbloodPack();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Attacking creatures you control get +1/+0.
        // OWN_CREATURES handles other creatures; SELF handles the source itself (no "other" in oracle text).
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, GrantScope.SELF, new PermanentIsAttackingPredicate()));

        // At the beginning of each upkeep, if no spells were cast last turn, transform Instigator Gang.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new NoSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "WildbloodPack";
    }
}
