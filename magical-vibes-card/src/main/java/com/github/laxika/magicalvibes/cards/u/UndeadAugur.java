package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "112")
public class UndeadAugur extends Card {

    public UndeadAugur() {
        // Whenever this creature or another Zombie you control dies, you draw a card and you lose
        // 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE), new DrawCardEffect(1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE), new LoseLifeEffect(1)));
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(1));
    }
}
