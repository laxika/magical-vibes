package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCreatureCardsWithHatchingCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

public class DragonKamisEgg extends Card {

    public DragonKamisEgg() {
        addEffect(EffectSlot.ON_DEATH, new MayCastExiledCreatureCardsWithHatchingCountersEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON),
                new MayCastExiledCreatureCardsWithHatchingCountersEffect()));
    }
}
