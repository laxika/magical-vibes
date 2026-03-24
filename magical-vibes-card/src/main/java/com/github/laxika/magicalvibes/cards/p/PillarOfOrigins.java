package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "241")
public class PillarOfOrigins extends Card {

    public PillarOfOrigins() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorChosenSubtypeCreatureManaEffect()),
                "{T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type."
        ));
    }
}
