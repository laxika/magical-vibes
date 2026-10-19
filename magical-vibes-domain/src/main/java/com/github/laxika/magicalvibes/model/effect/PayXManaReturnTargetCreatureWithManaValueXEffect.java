package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * "You may pay {X}. If you do, return target creature card with mana value X from your graveyard to
 * the battlefield." The creature card is targeted as the ability is put on the stack (any creature
 * card matching {@code filter}); X is chosen and paid on resolution, and the target is returned only
 * if its mana value equals X.
 *
 * @param filter additional card restriction, such as a creature subtype or excluding the source
 */
public record PayXManaReturnTargetCreatureWithManaValueXEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        CardPredicate creatureCard = filter == null
                ? new CardTypePredicate(CardType.CREATURE)
                : new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.CREATURE), filter));
        return TargetSpec.benign(TargetPredicates.graveyardCards(creatureCard,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
