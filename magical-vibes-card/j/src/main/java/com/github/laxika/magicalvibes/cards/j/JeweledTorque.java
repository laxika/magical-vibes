package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "301")
public class JeweledTorque extends Card {

    public JeweledTorque() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new MayPayManaEffect("{2}", new GainLifeEffect(2), "Pay {2} to gain 2 life?"));
    }
}
