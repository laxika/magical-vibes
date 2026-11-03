package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "139")
@CardRegistration(set = "TPR", collectorNumber = "107")
@CardRegistration(set = "VMA", collectorNumber = "124")
@CardRegistration(set = "SLD", collectorNumber = "826")
public class Kezzerdrix extends Card {

    public Kezzerdrix() {
        // Intervening-if: the "your opponents control no creatures" clause is checked both when the
        // upkeep trigger would go on the stack and again on resolution.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new OpponentControlsPermanent(new PermanentIsCreaturePredicate()))),
                new DealDamageToPlayersEffect(4, DamageRecipient.CONTROLLER));
    }
}
