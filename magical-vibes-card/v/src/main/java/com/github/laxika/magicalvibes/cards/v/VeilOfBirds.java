package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "106")
public class VeilOfBirds extends Card {

    public VeilOfBirds() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                new BecomeCreatureEffect(1, 1, CardSubtype.BIRD, Set.of(Keyword.FLYING)));
    }
}
