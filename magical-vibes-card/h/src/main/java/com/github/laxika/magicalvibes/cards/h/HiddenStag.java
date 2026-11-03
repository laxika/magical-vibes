package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeEnchantmentEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "265")
public class HiddenStag extends Card {

    public HiddenStag() {
        addTrigger(Triggers.opponentPlaysLand(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                new BecomeCreatureEffect(3, 2, List.of(CardSubtype.ELK, CardSubtype.BEAST)));
        addTrigger(Triggers.youPlayLand(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsCreature()),
                new BecomeEnchantmentEffect(true));
    }
}
