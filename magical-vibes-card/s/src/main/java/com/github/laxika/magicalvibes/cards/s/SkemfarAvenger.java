package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "109")
@CardRegistration(set = "KHM", collectorNumber = "355")
public class SkemfarAvenger extends Card {

    public SkemfarAvenger() {
        // Whenever another nontoken Elf or Berserker you control dies, you draw a card and you
        // lose 1 life.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        new CardSubtypePredicate(CardSubtype.BERSERKER)
                )),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
