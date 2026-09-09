package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "68")
public class LethalVapors extends Card {

    public LethalVapors() {
        // Whenever a creature enters, destroy it.
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.CREATURE),
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)));

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
