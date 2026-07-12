package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "124")
public class DeathPitOffering extends Card {

    public DeathPitOffering() {
        // When this enchantment enters, sacrifice all creatures you control.
        // Count of controlled creatures == matching creatures, so the forced-sacrifice handler
        // sacrifices them all with no choice prompt. The predicate is wrapped (not a bare
        // PermanentIsCreaturePredicate) so it routes through the multi-permanent path rather than
        // the single-creature "sacrifice a creature" primitive.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificePermanentsEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.CONTROLLER));

        // Creatures you control get +2/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES));
    }
}
