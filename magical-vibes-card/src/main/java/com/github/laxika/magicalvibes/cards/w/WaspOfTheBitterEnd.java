package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDestroyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "206")
public class WaspOfTheBitterEnd extends Card {

    public WaspOfTheBitterEnd() {
        // Flying (keyword, auto-loaded).
        // Whenever you cast a Bolas planeswalker spell, you may sacrifice this creature.
        // If you do, destroy target creature.
        // Target chosen as the trigger stacks (CR 603.3d); may/sacrifice at resolution (CR 603.5).
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.PLANESWALKER),
                        new CardSubtypePredicate(CardSubtype.BOLAS)
                )),
                List.of(new MayEffect(
                        new SacrificeSelfThenDestroyTargetEffect(),
                        "You may sacrifice this creature. If you do, destroy the target creature.")),
                null,
                TargetFilters.creature()
        ));
    }
}
