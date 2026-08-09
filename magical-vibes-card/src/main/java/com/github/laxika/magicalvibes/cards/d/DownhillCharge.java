package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "79")
public class DownhillCharge extends Card {

    public DownhillCharge() {
        // You may sacrifice a Mountain rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));

        // Target creature gets +X/+0 until end of turn, where X is the number of Mountains you control.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
