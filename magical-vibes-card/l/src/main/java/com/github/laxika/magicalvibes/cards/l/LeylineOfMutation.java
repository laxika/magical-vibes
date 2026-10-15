package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DSK", collectorNumber = "188")
public class LeylineOfMutation extends Card {

    public LeylineOfMutation() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Mutation on the battlefield?"
        ));
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect("{W}{U}{B}{R}{G}", null));
    }
}
