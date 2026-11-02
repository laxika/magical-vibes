package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class ValkmiraProtectorsShield extends Card {

    public ValkmiraProtectorsShield() {
        addStaticEffect(new PreventDamageFromOpponentSourcesEffect(1, true));
        addTrigger(Triggers.youBecomeTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(1));
        addTrigger(Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(1));
    }
}
