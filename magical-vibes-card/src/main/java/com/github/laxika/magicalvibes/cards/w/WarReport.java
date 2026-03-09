package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerMatchingPermanentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "26")
public class WarReport extends Card {

    public WarReport() {
        addEffect(EffectSlot.SPELL, new GainLifePerMatchingPermanentOnBattlefieldEffect(
                List.of(new PermanentIsCreaturePredicate(), new PermanentIsArtifactPredicate())
        ));
    }
}
