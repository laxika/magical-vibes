package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "80")
public class VengefulDead extends Card {

    public VengefulDead() {
        // Whenever this creature or another Zombie dies, each opponent loses 1 life.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)));
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
