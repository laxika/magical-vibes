package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.FirstSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "3")
public class TheLordOfPain extends Card {

    public TheLordOfPain() {
        addEffect(EffectSlot.STATIC, new OpponentsCantGainLifeEffect());
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new FirstSpellCastTriggerEffect(List.of(
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER))));
    }
}
