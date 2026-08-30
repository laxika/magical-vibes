package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromGraveyardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "TDM", collectorNumber = "127")
public class TersaLightshatter extends Card {

    public TersaLightshatter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardUpToThenDrawThatManyEffect(2));

        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new GraveyardCardThreshold(7, new CardTruePredicate()),
                new ExileRandomCardFromGraveyardMayPlayThisTurnEffect()));
    }
}
