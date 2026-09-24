package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraTurnSkipReplacementEffect;

import java.util.List;

@CardRegistration(set = "PZA", collectorNumber = "3")
public class TroubleInPairs extends Card {

    public TroubleInPairs() {
        addEffect(EffectSlot.STATIC, new OpponentExtraTurnSkipReplacementEffect());

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new OpponentAttacksWithAtLeastCreatures(2, false), new DrawCardEffect()));

        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2, new DrawCardEffect()));

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new NthSpellCastTriggerEffect(2, List.of(new DrawCardEffect())));
    }
}
