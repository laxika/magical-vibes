package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "152")
@CardRegistration(set = "HBG", collectorNumber = "185")
public class JadedSellSword extends Card {

    public JadedSellSword() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new TreasureManaSpentToCast()),
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.HASTE), GrantScope.SELF));
    }
}
