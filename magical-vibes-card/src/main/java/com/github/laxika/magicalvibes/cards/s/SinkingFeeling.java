package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "51")
public class SinkingFeeling extends Card {

    public SinkingFeeling() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // Enchanted creature doesn't untap during its controller's untap step.
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted())
                // Enchanted creature has "{1}, Put a -1/-1 counter on this creature: Untap this creature."
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{1}",
                                List.of(new PutCounterOnSourceCost(), new UntapPermanentsEffect(TapUntapScope.SELF)),
                                "{1}, Put a -1/-1 counter on this creature: Untap this creature."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ));
    }
}
