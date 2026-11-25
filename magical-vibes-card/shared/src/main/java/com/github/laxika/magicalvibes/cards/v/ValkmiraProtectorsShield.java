package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class ValkmiraProtectorsShield extends Card {

    public ValkmiraProtectorsShield() {
        addStaticEffect(new PreventDamageFromOpponentSourcesEffect(1, true));
        addTrigger(Triggers.youBecomeTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(1));
        addTrigger(Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(1));
    }
}
