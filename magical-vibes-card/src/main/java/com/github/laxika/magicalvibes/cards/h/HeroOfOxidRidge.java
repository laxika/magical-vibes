package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "MBS", collectorNumber = "66")
public class HeroOfOxidRidge extends Card {

    public HeroOfOxidRidge() {
        // Whenever Hero of Oxid Ridge attacks, creatures with power 1 or less can't block this turn.
        // (Haste and Battle cry are auto-loaded from Scryfall; battle cry is engine-handled.)
        addEffect(EffectSlot.ON_ATTACK, new CantBlockThisTurnEffect(new PermanentPowerAtMostPredicate(1)));
    }
}
