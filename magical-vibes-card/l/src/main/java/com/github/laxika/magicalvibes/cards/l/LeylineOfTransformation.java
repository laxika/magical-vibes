package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "DSK", collectorNumber = "63")
public class LeylineOfTransformation extends Card {

    public LeylineOfTransformation() {
        addStaticEffect(new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Transformation on the battlefield?"
        ));
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new GrantChosenSubtypeToOwnCreaturesEffect(true));
    }
}
