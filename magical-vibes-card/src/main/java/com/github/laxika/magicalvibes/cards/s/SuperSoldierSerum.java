package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "38")
public class SuperSoldierSerum extends Card {

    public SuperSoldierSerum() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2,
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.SOLDIER, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantSupertypeToEnchantedPermanentEffect(CardSupertype.LEGENDARY));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"), 0, 99)
                .addEffect(EffectSlot.ON_ATTACK, new AttachTargetEquipmentToTriggeringPermanentEffect())
                .addEffect(EffectSlot.ON_BLOCK, new AttachTargetEquipmentToTriggeringPermanentEffect());
    }
}
