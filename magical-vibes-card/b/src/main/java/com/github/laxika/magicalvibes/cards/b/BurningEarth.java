package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M14", collectorNumber = "130")
public class BurningEarth extends Card {

    public BurningEarth() {
        // Whenever a player taps a nonbasic land for mana, Burning Earth deals 1 damage to that player.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new DealDamageOnLandTapEffect(1,
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));
    }
}
