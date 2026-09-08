package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "83")
public class DavrielRogueShadowmage extends Card {

    public DavrielRogueShadowmage() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandAtMost(1),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "−1: Target player discards a card."
        ));
    }
}
