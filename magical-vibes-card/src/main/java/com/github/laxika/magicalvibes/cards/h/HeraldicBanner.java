package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "254")
public class HeraldicBanner extends Card {

    public HeraldicBanner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenColorEffect(1, 0));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."
        ));
    }
}
