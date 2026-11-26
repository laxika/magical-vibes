package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class RefractionElemental extends Card {

    public RefractionElemental() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessPaysLife(2));
        addTrigger(Triggers.youCastSpell().anySpell(), DealDamageEffect.to(Subjects.eachOpponent(), 2));
    }
}
