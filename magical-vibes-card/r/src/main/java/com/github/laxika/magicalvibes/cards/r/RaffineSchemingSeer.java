package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "213")
@CardRegistration(set = "SLZ", collectorNumber = "93")
@CardRegistration(set = "SLZ", collectorNumber = "214")
@CardRegistration(set = "SLZ", collectorNumber = "335")
public class RaffineSchemingSeer extends Card {

    public RaffineSchemingSeer() {
        // Ward {1}.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(1));

        target(TargetFilters.attackingCreature()).addTrigger(Triggers.youAttack(),
                new DrawDiscardAndConniveEffect(
                        new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER), true));
    }
}
