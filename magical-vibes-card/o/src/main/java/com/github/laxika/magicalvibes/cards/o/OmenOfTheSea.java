package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "58")
public class OmenOfTheSea extends Card {

    public OmenOfTheSea() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2), new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new SacrificeSelfCost(), new ScryEffect(2)),
                "{2}{U}, Sacrifice this enchantment: Scry 2."
        ));
    }
}
