package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "120")
public class SanctumSeeker extends Card {

    public SanctumSeeker() {
        // Whenever a Vampire you control attacks, each opponent loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new SubtypeConditionalEffect(CardSubtype.VAMPIRE,
                        new EachOpponentLosesLifeEffect(1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new SubtypeConditionalEffect(CardSubtype.VAMPIRE,
                        new GainLifeEffect(1)));
    }
}
