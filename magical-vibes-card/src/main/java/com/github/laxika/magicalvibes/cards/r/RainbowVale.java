package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;

@CardRegistration(set = "FEM", collectorNumber = "99")
@CardRegistration(set = "FEM", collectorNumber = "142")
public class RainbowVale extends Card {

    public RainbowVale() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new RegisterDelayedChooseOpponentGainsControlOfSourceEffect());
    }
}
