package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "68")
public class LethalVapors extends Card {

    public LethalVapors() {
        // Whenever a creature enters, destroy it.
        addTrigger(Triggers.enters(new PermanentIsCreaturePredicate(), ObjectRelation.ANY),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));

        // {0}: Destroy this enchantment. You skip your next turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new DestroyReferencedPermanentEffect(PermanentReference.SOURCE),
                        new SkipNextEffect(SkipKind.TURN)
                ),
                "{0}: Destroy this enchantment. You skip your next turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
