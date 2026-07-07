package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "199")
public class RustedRelic extends Card {

    public RustedRelic() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Metalcraft(), 
                new AnimatePermanentsEffect(5, 5, List.of(CardSubtype.GOLEM), Set.of())));
    }
}
