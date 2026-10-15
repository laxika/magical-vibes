package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "303")
@CardRegistration(set = "MB2", collectorNumber = "539")
public class WisedraftersWill extends Card {

    public WisedraftersWill() {
        addEffect(EffectSlot.STATIC, new RevealOpponentHandsEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{U}, Sacrifice Wisedrafter's Will: Draw a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{U}{U}, Sacrifice Wisedrafter's Will: Counter target spell."
        ));
    }
}
