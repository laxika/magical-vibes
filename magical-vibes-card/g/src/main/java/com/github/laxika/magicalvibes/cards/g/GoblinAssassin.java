package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "95")
public class GoblinAssassin extends Card {

    public GoblinAssassin() {
        EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect effect =
                new EachPlayerFlipsCoinAndTailsSacrificesCreatureEffect();
        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.GOBLIN), effect));
    }
}
