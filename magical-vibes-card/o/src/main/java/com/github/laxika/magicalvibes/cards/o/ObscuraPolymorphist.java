package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YSNC", collectorNumber = "24")
public class ObscuraPolymorphist extends Card {

    public ObscuraPolymorphist() {
        // When this creature enters the battlefield, exile target creature. Its controller seeks
        // a creature card.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentThenEffect(
                        new SeekEffect(new CardTypePredicate(CardType.CREATURE)),
                        ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
