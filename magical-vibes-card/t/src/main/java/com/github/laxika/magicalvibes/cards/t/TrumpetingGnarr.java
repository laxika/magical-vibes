package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "213")
public class TrumpetingGnarr extends Card {

    public TrumpetingGnarr() {
        addCastingOption(AlternateHandCast.mutate("{3}{G/U}{G/U}", new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)))),
                "Mutate requires a non-Human creature with the spell's owner")));
        addEffect(EffectSlot.ON_SELF_MUTATES, new CreateTokenEffect(
                "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()));
    }
}
