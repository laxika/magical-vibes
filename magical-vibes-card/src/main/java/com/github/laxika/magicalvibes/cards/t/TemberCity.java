package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "OPCA", collectorNumber = "79")
public class TemberCity extends Card {

    public TemberCity() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new DealDamageOnLandTapEffect(1));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new SacrificePermanentsEffect(
                1, new PermanentNotPredicate(new PermanentIsLandPredicate()), SacrificeRecipient.EACH_OPPONENT));
    }
}
