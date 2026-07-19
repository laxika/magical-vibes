package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "32")
public class ParasiticStrix extends Card {

    public ParasiticStrix() {
        // Flying is auto-loaded from Scryfall.
        // When this creature enters, if you control a black permanent, target player loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                                new GainLifeEffect(2))));
    }
}
