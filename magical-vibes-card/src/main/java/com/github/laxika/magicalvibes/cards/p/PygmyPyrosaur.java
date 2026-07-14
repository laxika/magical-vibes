package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "208")
public class PygmyPyrosaur extends Card {

    public PygmyPyrosaur() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)), "{R}: Pygmy Pyrosaur gets +1/+0 until end of turn."));
    }
}
