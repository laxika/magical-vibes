package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "166")
public class PhageTheUntouchable extends Card {

    public PhageTheUntouchable() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseGameIfNotCastFromHandEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new DestroyTargetPermanentEffect(Set.of(CardType.CREATURE), true));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerLosesGameEffect(null));
    }
}
