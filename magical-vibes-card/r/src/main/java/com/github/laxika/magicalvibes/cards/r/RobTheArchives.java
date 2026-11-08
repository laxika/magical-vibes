package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CasualtyCostPaid;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "122")
public class RobTheArchives extends Card {

    public RobTheArchives() {
        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()),
                new CopyTriggeringSpellEffect());
        addCastingCost(new CasualtyCost(1));
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false));
    }
}
