package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "OTC", collectorNumber = "253")
public class BladegriffPrototype extends Card {

    public BladegriffPrototype() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
