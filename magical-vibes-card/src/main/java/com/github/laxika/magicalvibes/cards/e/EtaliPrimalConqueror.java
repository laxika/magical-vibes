package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "137")
public class EtaliPrimalConqueror extends Card {

    public EtaliPrimalConqueror() {
        setBackFaceCard(new EtaliPrimalSickness());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{9}{G/P}",
                List.of(new TransformSelfEffect()),
                "{9}{G/P}: Transform Etali. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "EtaliPrimalSickness";
    }
}
