package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "HOB", collectorNumber = "48")
public class MirkwoodMeditator extends Card {

    public MirkwoodMeditator() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new SetBasePowerToughnessEffect(4, 2, GrantScope.SELF),
                        "Have this creature's base power and toughness become 4/2 until end of turn?"));
    }
}
