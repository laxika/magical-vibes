package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventTransformEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "141")
public class Immerwolf extends Card {

    public Immerwolf() {
        // Each other creature you control that's a Wolf or a Werewolf gets +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF))));

        // Non-Human Werewolves you control can't transform.
        addEffect(EffectSlot.STATIC, new PreventTransformEffect(new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))))));
    }
}
