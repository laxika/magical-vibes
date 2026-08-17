package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "151")
public class Skullcage extends Card {

    public Skullcage() {
        // The "unless" clause is checked only as the triggered ability resolves, so the trigger
        // must still be put on the stack when the opponent has exactly three or four cards.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, ConditionalEffect.unless(
                new AnyOf(List.of(new ActivePlayerHandAtMost(2), new ActivePlayerHandAtLeast(5))),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)));
    }
}
