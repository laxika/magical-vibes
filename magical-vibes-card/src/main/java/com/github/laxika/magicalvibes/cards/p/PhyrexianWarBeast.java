package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ALL", collectorNumber = "127a")
@CardRegistration(set = "ALL", collectorNumber = "127b")
public class PhyrexianWarBeast extends Card {

    public PhyrexianWarBeast() {
        // When this creature leaves the battlefield, sacrifice a land and this creature deals 1 damage to you.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
