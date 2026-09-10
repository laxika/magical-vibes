package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DSK", collectorNumber = "63")
public class LeylineOfTransformation extends Card {

    public LeylineOfTransformation() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Transformation on the battlefield?"
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantChosenSubtypeToOwnCreaturesEffect(true));
    }
}
