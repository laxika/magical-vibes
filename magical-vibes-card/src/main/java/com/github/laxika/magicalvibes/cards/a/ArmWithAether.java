package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect;

@CardRegistration(set = "NPH", collectorNumber = "28")
public class ArmWithAether extends Card {

    public ArmWithAether() {
        addEffect(EffectSlot.SPELL, new GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect());
    }
}
