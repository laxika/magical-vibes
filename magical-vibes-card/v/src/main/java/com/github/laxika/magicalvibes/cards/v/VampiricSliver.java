package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "140")
public class VampiricSliver extends Card {

    public VampiricSliver() {
        // All Sliver creatures have the ability to grow when a creature they damaged dies.
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new PutCountersOnSourceEffect(1, 1, 1),
                GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new PutCountersOnSourceEffect(1, 1, 1),
                GrantScope.SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
