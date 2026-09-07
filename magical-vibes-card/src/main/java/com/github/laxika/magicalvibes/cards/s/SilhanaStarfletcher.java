package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "95")
public class SilhanaStarfletcher extends Card {

    public SilhanaStarfletcher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."));
    }
}
