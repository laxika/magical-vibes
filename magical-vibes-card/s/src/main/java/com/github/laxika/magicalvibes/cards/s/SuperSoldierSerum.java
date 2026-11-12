package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "38")
public class SuperSoldierSerum extends Card {

    public SuperSoldierSerum() {
        setAttackTriggerOwnedByAura(true);
        target(TargetFilters.creature());
        addStaticEffect(new StaticBoostEffect(2, 2,
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), GrantScope.ENCHANTED_CREATURE));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.SOLDIER, GrantScope.ENCHANTED_CREATURE));
        addStaticEffect(new GrantSupertypeToEnchantedPermanentEffect(CardSupertype.LEGENDARY));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 99)
                .addTrigger(Triggers.attacks(), new AttachTargetEquipmentToTriggeringPermanentEffect())
                .addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE,
                        new AttachTargetEquipmentToTriggeringPermanentEffect());
    }
}
