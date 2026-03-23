package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapSubtypeBoostSelfAndDamageDefenderEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "180")
public class MyrBattlesphere extends Card {

    public MyrBattlesphere() {
        // When this creature enters, create four 1/1 colorless Myr artifact creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                4, "Myr", 1, 1, null, List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)));

        // Whenever this creature attacks, you may tap X untapped Myr you control.
        // If you do, this creature gets +X/+0 until end of turn and deals X damage
        // to the player or planeswalker it's attacking.
        addEffect(EffectSlot.ON_ATTACK, new TapSubtypeBoostSelfAndDamageDefenderEffect(CardSubtype.MYR));
    }
}
