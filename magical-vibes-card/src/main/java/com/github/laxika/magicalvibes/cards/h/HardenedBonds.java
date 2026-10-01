package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyBoostSoughtCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YTDM", collectorNumber = "16")
public class HardenedBonds extends Card {

    public HardenedBonds() {
        addEffect(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE,
                new OncePerTurnTriggerEffect(new SeekLibraryAndPerpetuallyBoostSoughtCardEffect(
                        new CardTypePredicate(CardType.CREATURE), 1, 1)));
    }
}
