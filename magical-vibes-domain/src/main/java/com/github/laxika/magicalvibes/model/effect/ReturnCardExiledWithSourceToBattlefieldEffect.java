package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Returns one card exiled "with" the source permanent to the battlefield under the ability
 * controller's control (CR 110.2a). With several such cards the controller chooses which one; only
 * the chosen card leaves exile. Battlefield sibling of {@link PutCardExiledWithSourceIntoHandEffect}.
 *
 * <p>Purgatory's upkeep trigger, wrapped in a {@code MayPayManaEffect("{4}", 2, …)} for the
 * "you may pay {4} and 2 life" gate. The filter, mana-value-X restriction, and persistent subtype
 * grant are used by Ashiok, Nightmare Weaver's loyalty ability. The targeted form additionally
 * supports tapped entry and extra +1/+1 counters.
 */
public record ReturnCardExiledWithSourceToBattlefieldEffect(
        CardPredicate filter, boolean requiresManaValueEqualsX, CardSubtype grantedSubtype,
        boolean targeted, boolean entersTapped, int additionalPlusOnePlusOneCounters)
        implements CardEffect {

    public ReturnCardExiledWithSourceToBattlefieldEffect(CardPredicate filter,
                                                         boolean requiresManaValueEqualsX,
                                                         CardSubtype grantedSubtype) {
        this(filter, requiresManaValueEqualsX, grantedSubtype, false, false, 0);
    }

    public ReturnCardExiledWithSourceToBattlefieldEffect() {
        this(null, false, null, false, false, 0);
    }

    public static ReturnCardExiledWithSourceToBattlefieldEffect targetedCreature(
            boolean entersTapped, int additionalPlusOnePlusOneCounters) {
        return new ReturnCardExiledWithSourceToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE),
                false, null, true, entersTapped, additionalPlusOnePlusOneCounters);
    }

    @Override
    public TargetSpec targetSpec() {
        if (!targeted) {
            return TargetSpec.NONE;
        }
        CardPredicate targetFilter = filter == null ? new CardTruePredicate() : filter;
        return TargetSpec.benign(TargetPredicates.exiledCards(targetFilter));
    }
}
