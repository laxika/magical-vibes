package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "185")
@CardRegistration(set = "4ED", collectorNumber = "151")
public class Paralyze extends Card {

    public Paralyze() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // When this Aura enters, tap enchanted creature.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET))

                // Enchanted creature doesn't untap during its controller's untap step.
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted())

                // At the beginning of the upkeep of enchanted creature's controller, that player
                // may pay {4}. If the player does, untap the creature.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new MayPayManaEffect("{4}", new UntapEquippedCreatureEffect(),
                                "pay {4} to untap the creature", true));
    }
}
