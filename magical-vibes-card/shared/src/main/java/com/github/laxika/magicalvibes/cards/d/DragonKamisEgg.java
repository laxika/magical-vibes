package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCreatureCardsWithHatchingCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DragonKamisEgg extends Card {

    public DragonKamisEgg() {
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new MayCastExiledCreatureCardsWithHatchingCountersEffect());
    }
}
