package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "148")
@CardRegistration(set = "FIN", collectorNumber = "387")
@CardRegistration(set = "FIN", collectorNumber = "463")
@CardRegistration(set = "FIN", collectorNumber = "532")
public class PromptoArgentum extends Card {

    public PromptoArgentum() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(4)),
                CreateTokenEffect.ofTreasureToken(1));
    }
}
