package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "234")
public class QuirionElves extends Card {

    public QuirionElves() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardChosenColorManaEffect()),
                "{T}: Add one mana of the chosen color."));
    }
}
