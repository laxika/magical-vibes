package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoHandThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "5")
public class QuicksilverServitor extends Card {

    public QuicksilverServitor() {
        // Ward {2}.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        addTrigger(Triggers.youCastSpell().withFilter(new CardPutIntoHandThisTurnPredicate()), new ProliferateEffect());
    }
}
