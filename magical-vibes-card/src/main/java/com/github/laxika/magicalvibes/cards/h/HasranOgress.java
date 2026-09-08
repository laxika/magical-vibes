package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "34")
public class HasranOgress extends Card {

    public HasranOgress() {
        // Whenever this creature attacks, it deals 3 damage to you unless you pay {2}.
        addEffect(EffectSlot.ON_ATTACK,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{2}"),
                        List.of(new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)),
                        true));
    }
}
