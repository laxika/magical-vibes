package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "618")
public class DaughterOfTheDeep extends Card {

    public DaughterOfTheDeep() {
        // Whenever you draw your second card each turn, create a 1/1 blue Merfolk creature token.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new CreateTokenEffect("Merfolk", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.MERFOLK), Set.of(), Set.of()));

        // {U}, {T}: Target Merfolk can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{U}", List.of(MakeCreatureUnblockableEffect.forTargetPermanent(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MERFOLK)))),
                "{U}, {T}: Target Merfolk can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MERFOLK)),
                        "Target must be a Merfolk creature"
                )));
    }
}
