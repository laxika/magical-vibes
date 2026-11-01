package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "207")
@CardRegistration(set = "DBL", collectorNumber = "474")
public class LaidToRest extends Card {

    public LaidToRest() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.HUMAN),
                new DrawCardEffect()));
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                new GainLifeEffect(2)));
    }
}
