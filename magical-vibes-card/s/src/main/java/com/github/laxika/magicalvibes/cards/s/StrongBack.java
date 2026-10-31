package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceAuraCastCostForTargetingAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostForTargetingAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "83")
@CardRegistration(set = "PIP", collectorNumber = "402")
@CardRegistration(set = "PIP", collectorNumber = "611")
@CardRegistration(set = "PIP", collectorNumber = "930")
public class StrongBack extends Card {

    public StrongBack() {
        Scaled twicePerAttachment = new Scaled(new AttachmentsOnSource(true, true), 2);
        target(TargetFilters.creature())
                .addStaticEffect(new ReduceEquipCostForTargetingAttachedPermanentEffect(3))
                .addStaticEffect(new ReduceAuraCastCostForTargetingAttachedPermanentEffect(3))
                .addStaticEffect(new AttachedBoostEffect(
                        twicePerAttachment, twicePerAttachment, GrantScope.ENCHANTED_CREATURE, true));
    }
}
