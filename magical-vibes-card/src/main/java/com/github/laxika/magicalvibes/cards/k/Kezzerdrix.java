package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TMP", collectorNumber = "139")
public class Kezzerdrix extends Card {

    public Kezzerdrix() {
        // Intervening-if: the "your opponents control no creatures" clause is checked both when the
        // upkeep trigger would go on the stack and again on resolution.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new OpponentControlsPermanent(new PermanentIsCreaturePredicate())),
                new DealDamageToPlayersEffect(4, DamageRecipient.CONTROLLER)));
    }
}
