package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "49")
public class ChaoticBacklash extends Card {

    public ChaoticBacklash() {
        // Deals damage to target player equal to twice the number of white and/or blue permanents they control.
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new Scaled(new PermanentCount(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE)),
                        CountScope.TARGET_PLAYER), 2),
                DamageRecipient.TARGET_PLAYER));
    }
}
