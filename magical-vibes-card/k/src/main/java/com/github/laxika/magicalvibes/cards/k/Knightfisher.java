package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "55")
public class Knightfisher extends Card {

    public Knightfisher() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.NONTOKEN_CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.BIRD))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new CreateTokenEffect("Fish", 1, 1, CardColor.BLUE, List.of(CardSubtype.FISH), Set.of(), Set.of()));
    }
}
