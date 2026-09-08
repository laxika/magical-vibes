package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "59")
public class KalastriaHighborn extends Card {

    private static final MayPayManaEffect DEATH_TRIGGER = new MayPayManaEffect("{B}",
            SequenceEffect.of(
                    new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                    new GainLifeEffect(2)),
            "Pay {B}?");

    public KalastriaHighborn() {
        // Whenever this creature or another Vampire you control dies, you may pay {B}. If you do,
        // target player loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.VAMPIRE), DEATH_TRIGGER));
    }
}
