package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackOpponentWithMostLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "8")
@CardRegistration(set = "MSC", collectorNumber = "291")
public class GalactusDevourerOfWorlds extends Card {

    public GalactusDevourerOfWorlds() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentEffect());
        addEffect(EffectSlot.STATIC,
                new MustAttackOpponentWithMostLifeEffect("Silver Surfer, Galactus's Herald"));
    }
}
