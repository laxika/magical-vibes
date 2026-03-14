package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M10", collectorNumber = "164")
public class YawningFissure extends Card {

    public YawningFissure() {
        addEffect(EffectSlot.SPELL, new EachOpponentSacrificesPermanentsEffect(1, new PermanentIsLandPredicate()));
    }
}
