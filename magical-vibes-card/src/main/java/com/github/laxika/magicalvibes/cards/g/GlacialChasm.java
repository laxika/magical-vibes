package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ICE", collectorNumber = "353")
public class GlacialChasm extends Card {

    public GlacialChasm() {
        // Cumulative upkeep—Pay 2 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.life(2));

        // When this land enters, sacrifice a land. Glacial Chasm itself is a legal choice.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));

        // Creatures you control can't attack — no creature is ever exempt.
        addEffect(EffectSlot.STATIC, new ControlledCreaturesCantAttackUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentTruePredicate())));

        // Prevent all damage that would be dealt to you.
        addEffect(EffectSlot.STATIC, new PreventAllDamageToControllerEffect());
    }
}
