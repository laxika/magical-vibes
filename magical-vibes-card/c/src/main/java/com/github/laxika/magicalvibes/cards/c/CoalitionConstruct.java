package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect;

@CardRegistration(set = "YDMU", collectorNumber = "30")
public class CoalitionConstruct extends Card {

    public CoalitionConstruct() {
        // As Coalition Construct enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // Coalition Construct is the chosen type in addition to its other types.
        addEffect(EffectSlot.STATIC, GrantChosenSubtypeToOwnCreaturesEffect.toSelf());

        // Other creatures you control of the chosen type and creature cards of that type in your
        // hand perpetually get +1/+1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect(1, 1));
    }
}
