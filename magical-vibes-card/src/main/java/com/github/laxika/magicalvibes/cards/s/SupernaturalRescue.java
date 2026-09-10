package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "37")
public class SupernaturalRescue extends Card {

    public SupernaturalRescue() {
        // This spell has flash as long as you control a Spirit.
        setFlashCastCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));

        // Enchant creature you control. Enchanted creature gets +1/+2.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE));

        // When you cast this spell, tap up to two target creatures you don't control.
        target(TargetFilters.creatureAnOpponentControls(), 0, 2)
                .addEffect(EffectSlot.ON_SELF_CAST, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
