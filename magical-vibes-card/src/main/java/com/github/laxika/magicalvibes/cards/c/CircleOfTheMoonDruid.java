package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "AFR", collectorNumber = "177")
public class CircleOfTheMoonDruid extends Card {

    public CircleOfTheMoonDruid() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantSubtypeEffect(CardSubtype.BEAR, GrantScope.SELF, true)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new SetBasePowerToughnessEffect(4, 2, GrantScope.SELF)));
    }
}
