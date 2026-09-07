package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "176")
public class OmniCheesePizza extends Card {

    public OmniCheesePizza() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{1}, {T}, Sacrifice this artifact: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this artifact: You gain 3 life."
        ));
    }
}
