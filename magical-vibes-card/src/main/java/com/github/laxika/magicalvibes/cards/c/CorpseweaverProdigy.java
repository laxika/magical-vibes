package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;

@CardRegistration(set = "YSOS", collectorNumber = "5")
public class CorpseweaverProdigy extends Card {

    public CorpseweaverProdigy() {
        // If a creature an opponent controls would die, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileOpponentCreaturesInsteadOfDyingEffect());

        // Infusion — At the beginning of your second main phase, if you gained life this turn,
        // conjure a card named Bridge from Below into your graveyard.
        addEffect(EffectSlot.SECOND_MAIN_PHASE_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(), new ConjureCardToGraveyardEffect("FUT", "81")));
    }
}
