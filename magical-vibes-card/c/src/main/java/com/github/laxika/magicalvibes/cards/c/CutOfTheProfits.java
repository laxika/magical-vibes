package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CasualtyCostPaid;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "72")
public class CutOfTheProfits extends Card {

    public CutOfTheProfits() {
        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()),
                new CopyTriggeringSpellEffect());
        addCastingCost(new CasualtyCost(3));
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER));
    }
}
