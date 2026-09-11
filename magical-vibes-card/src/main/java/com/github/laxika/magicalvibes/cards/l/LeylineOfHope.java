package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;

@CardRegistration(set = "DSK", collectorNumber = "18")
public class LeylineOfHope extends Card {

    public LeylineOfHope() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Hope on the battlefield?"
        ));
        addEffect(EffectSlot.STATIC, new AdditionalLifeGainEffect(1));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 7),
                new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES)));
    }
}
