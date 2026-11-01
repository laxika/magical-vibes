package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCreatureCardsWithHatchingCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DragonKamisEgg extends Card {

    public DragonKamisEgg() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayCastExiledCreatureCardsWithHatchingCountersEffect());
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON),
                new MayCastExiledCreatureCardsWithHatchingCountersEffect()));
    }
}
