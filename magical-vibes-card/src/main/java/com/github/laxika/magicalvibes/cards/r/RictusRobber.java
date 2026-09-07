package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "102")
public class RictusRobber extends Card {

    public RictusRobber() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new Morbid(),
                new CreateTokenEffect("Zombie Rogue", 2, 2, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.BLACK),
                        List.of(CardSubtype.ZOMBIE, CardSubtype.ROGUE))));
    }
}
