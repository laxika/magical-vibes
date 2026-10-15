package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.FractionalLifeLossRecipient;
import com.github.laxika.magicalvibes.model.effect.StartSubgameEffect;

@CardRegistration(set = "ARN", collectorNumber = "10")
public class Shahrazad extends Card {
    public Shahrazad() {
        addEffect(EffectSlot.SPELL, new StartSubgameEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerLosesFractionOfLifeRoundedUpEffect(
                2, FractionalLifeLossRecipient.SUBGAME_NON_WINNERS));
    }
}
