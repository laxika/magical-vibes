package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "95")
public class GoblinAssassin extends Card {

    public GoblinAssassin() {
        EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect effect =
                new EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect();
        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))),
                        ObjectRelation.ANOTHER),
                effect);
    }
}
