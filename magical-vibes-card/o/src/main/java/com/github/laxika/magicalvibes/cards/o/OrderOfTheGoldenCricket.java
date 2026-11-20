package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "19")
public class OrderOfTheGoldenCricket extends Card {

    public OrderOfTheGoldenCricket() {
        // Whenever this creature attacks, you may pay {W}. If you do, it gains flying until end of turn.
        addTrigger(Triggers.attacks(),
                new MayPayManaEffect("{W}",
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.source()),
                        "Pay {W} to give this creature flying until end of turn?"));
    }
}
