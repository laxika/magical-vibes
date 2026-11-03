package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "260")
public class HiddenAncients extends Card {

    public HiddenAncients() {
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                new BecomeCreatureEffect(5, 5, CardSubtype.TREEFOLK));
    }
}
