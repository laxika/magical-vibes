package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "708")
public class WandasVision extends Card {

    public WandasVision() {
        addTrigger(Triggers.youCastSpell(), SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(new ExileTopUntilNonlandMayCastWithoutPayingManaEffect())));
    }
}
