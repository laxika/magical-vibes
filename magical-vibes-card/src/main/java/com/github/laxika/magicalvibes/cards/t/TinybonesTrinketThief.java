package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWithEmptyHandLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1407")
public class TinybonesTrinketThief extends Card {

    public TinybonesTrinketThief() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new AnOpponentDiscardedCardThisTurn(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}",
                List.of(new EachOpponentWithEmptyHandLosesLifeEffect(10)),
                "{4}{B}{B}: Each opponent with no cards in hand loses 10 life."));
    }
}
