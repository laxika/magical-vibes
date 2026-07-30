package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;

@CardRegistration(set = "M12", collectorNumber = "201")
public class AdaptiveAutomaton extends Card {

    public AdaptiveAutomaton() {
        // As this creature enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // This creature is the chosen type in addition to its other types.
        addEffect(EffectSlot.STATIC, GrantChosenSubtypeToOwnCreaturesEffect.toSelf());

        // Other creatures you control of the chosen type get +1/+1.
        addEffect(EffectSlot.STATIC, BoostCreaturesOfChosenSubtypeEffect.otherOwnCreatures(1, 1));
    }
}
