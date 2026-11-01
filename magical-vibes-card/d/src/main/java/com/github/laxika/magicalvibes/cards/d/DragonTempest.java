package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "136")
@CardRegistration(set = "IMA", collectorNumber = "125")
@CardRegistration(set = "GN3", collectorNumber = "74")
@CardRegistration(set = "TDC", collectorNumber = "94")
@CardRegistration(set = "C17", collectorNumber = "134")
@CardRegistration(set = "SCD", collectorNumber = "133")
@CardRegistration(set = "FDC", collectorNumber = "150")
public class DragonTempest extends Card {

    public DragonTempest() {
        // Whenever a creature you control with flying enters, it gains haste until end of turn.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE))));

        // Whenever a Dragon you control enters, this enchantment deals X damage to any target,
        // where X is the number of Dragons you control.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON),
                        new DealDamageToAnyTargetEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON), CountScope.CONTROLLER))));
    }
}
