package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageBySelfEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "287")
public class BarbedWire extends Card {

    public BarbedWire() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ACTIVE_PLAYER));
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new PreventNextDamageBySelfEffect()),
                "{2}: Prevent the next 1 damage that would be dealt by this artifact this turn."));
    }
}
