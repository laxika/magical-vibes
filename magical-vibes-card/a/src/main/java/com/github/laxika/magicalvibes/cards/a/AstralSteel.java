package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "2")
public class AstralSteel extends Card {

    public AstralSteel() {
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 2));
    }
}
