package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "99")
public class DawnhandEulogist extends Card {

    public DawnhandEulogist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new ConditionalEffect(
                        new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.ELF)),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(2))
                )
        ));
    }
}
