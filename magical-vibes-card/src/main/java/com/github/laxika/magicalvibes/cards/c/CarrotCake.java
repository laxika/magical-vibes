package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnlyEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "7")
public class CarrotCake extends Card {

    public CarrotCake() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, rabbitAndScry());
        addEffect(EffectSlot.ON_DEATH, new SacrificeOnlyEffect(rabbitAndScry()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this artifact: You gain 3 life."
        ));
    }

    private static SequenceEffect rabbitAndScry() {
        return SequenceEffect.of(
                new CreateTokenEffect("Rabbit", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.RABBIT), Set.of(), Set.of()),
                new ScryEffect(1)
        );
    }
}
