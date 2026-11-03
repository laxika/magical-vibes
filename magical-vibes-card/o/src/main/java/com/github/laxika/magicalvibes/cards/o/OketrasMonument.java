package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "233")
@CardRegistration(set = "AKR", collectorNumber = "277")
@CardRegistration(set = "DMC", collectorNumber = "188")
public class OketrasMonument extends Card {

    public OketrasMonument() {
        // White creature spells you cast cost {1} less to cast.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardTypePredicate(CardType.CREATURE)
                )), 1, CostModificationScope.SELF));

        // Whenever you cast a creature spell, create a 1/1 white Warrior creature token with vigilance.
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new CreateTokenEffect(1, "Warrior", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.WARRIOR), Set.of(Keyword.VIGILANCE), Set.of()));
    }
}
