package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "190")
public class TurtleLair extends Card {

    private static final Set<CardSubtype> NINJA_OR_TURTLE = Set.of(CardSubtype.NINJA, CardSubtype.TURTLE);
    private static final PermanentPredicate NINJA_OR_TURTLE_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasAnySubtypePredicate(NINJA_OR_TURTLE)));

    public TurtleLair() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add one mana of any color. Spend this mana only to cast a Ninja or Turtle spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardAnyColorManaEffect.forSpellSubtypes(1, NINJA_OR_TURTLE)),
                "{T}: Add one mana of any color. Spend this mana only to cast a Ninja or Turtle spell."
        ));

        // {3}, {T}: Target Ninja or Turtle can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{3}, {T}: Target Ninja or Turtle can't be blocked this turn.",
                new PermanentPredicateTargetFilter(NINJA_OR_TURTLE_CREATURE, "Target must be a Ninja or Turtle creature")
        ));
    }
}
