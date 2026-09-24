package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "57")
public class HaythamKenway extends Card {

    public HaythamKenway() {
        PermanentHasSubtypePredicate knights = new PermanentHasSubtypePredicate(CardSubtype.KNIGHT);

        addEffect(EffectSlot.STATIC,
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.ASSASSIN)));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES, knights));
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(
                        new ProtectionFromSubtypesEffect(Set.of(CardSubtype.ASSASSIN)),
                        GrantScope.OWN_CREATURES, knights));

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
