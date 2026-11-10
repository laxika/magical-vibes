package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "36")
@CardRegistration(set = "FIN", collectorNumber = "359")
public class SummonKnightsOfRound extends Card {

    private static final PermanentAllOfPredicate OTHER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    private static final CreateTokenEffect KNIGHTS = new CreateTokenEffect(
            3, "Knight", 2, 2, CardColor.WHITE, List.of(CardSubtype.KNIGHT), Set.of(), Set.<CardType>of());

    public SummonKnightsOfRound() {
        addTrigger(Triggers.chapter(1), KNIGHTS);
        addTrigger(Triggers.chapter(2), KNIGHTS);
        addTrigger(Triggers.chapter(3), KNIGHTS);
        addTrigger(Triggers.chapter(4), KNIGHTS);
        addTrigger(Triggers.chapter(5),
                new BoostAllOwnCreaturesEffect(2, 2, OTHER_CREATURES),
                new PutCounterOnEachControlledPermanentEffect(CounterType.INDESTRUCTIBLE, 1, OTHER_CREATURES));
    }
}
