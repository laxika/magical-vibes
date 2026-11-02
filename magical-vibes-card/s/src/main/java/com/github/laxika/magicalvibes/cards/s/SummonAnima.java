package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "120")
@CardRegistration(set = "FIN", collectorNumber = "364")
public class SummonAnima extends Card {

    public SummonAnima() {
        addTrigger(Triggers.chapter(1),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
        addTrigger(Triggers.chapter(2),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
        addTrigger(Triggers.chapter(3),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
        addTrigger(Triggers.chapter(4), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.EACH_OPPONENT),
                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT)));
    }
}
