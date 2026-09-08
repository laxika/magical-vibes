package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "255")
public class GauntletOfPower extends Card {

    public GauntletOfPower() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenColorEffect(1, 1));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddExtraManaOfChosenColorOnLandTapEffect(false,
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)));
    }
}
