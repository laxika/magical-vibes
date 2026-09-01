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
 * When {@code returnAtRandom} is true, one matching card is selected at random instead.
 *
 * <p>Purgatory's upkeep trigger, wrapped in a {@code MayPayManaEffect("{4}", 2, …)} for the
 * "you may pay {4} and 2 life" gate. The filter, mana-value-X restriction, and persistent subtype
 * grant are used by Ashiok, Nightmare Weaver's loyalty ability.
 */
public record ReturnCardExiledWithSourceToBattlefieldEffect(
        CardPredicate filter, boolean requiresManaValueEqualsX, CardSubtype grantedSubtype,
        boolean enterTapped, boolean enterAttacking,
        boolean returnAtRandom, boolean targeted, int additionalPlusOnePlusOneCounters)
        implements CardEffect {

    public ReturnCardExiledWithSourceToBattlefieldEffect() {
        this(null, false, null, false, false, false, false, 0);
    }

    public ReturnCardExiledWithSourceToBattlefieldEffect(CardPredicate filter,
                                                         boolean requiresManaValueEqualsX,
                                                         CardSubtype grantedSubtype) {
        this(filter, requiresManaValueEqualsX, grantedSubtype, false, false, false, false, 0);
    }

    public ReturnCardExiledWithSourceToBattlefieldEffect(CardPredicate filter,
                                                         boolean requiresManaValueEqualsX,
                                                         CardSubtype grantedSubtype,
                                                         boolean enterTapped,
                                                         boolean enterAttacking) {
        this(filter, requiresManaValueEqualsX, grantedSubtype,
                enterTapped, enterAttacking, false, false, 0);
    }

    public ReturnCardExiledWithSourceToBattlefieldEffect(
            CardPredicate filter, boolean requiresManaValueEqualsX, CardSubtype grantedSubtype,
            boolean enterTapped, boolean enterAttacking, boolean returnAtRandom) {
        this(filter, requiresManaValueEqualsX, grantedSubtype,
                enterTapped, enterAttacking, returnAtRandom, false, 0);
    }

    public ReturnCardExiledWithSourceToBattlefieldEffect(
            CardPredicate filter, boolean requiresManaValueEqualsX, CardSubtype grantedSubtype,
            boolean enterTapped, boolean enterAttacking, boolean targeted,
            int additionalPlusOnePlusOneCounters) {
        this(filter, requiresManaValueEqualsX, grantedSubtype, enterTapped, enterAttacking,
                false, targeted, additionalPlusOnePlusOneCounters);
    }

    public static ReturnCardExiledWithSourceToBattlefieldEffect targetedCreature(
            boolean enterTapped, int additionalPlusOnePlusOneCounters) {
        return new ReturnCardExiledWithSourceToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), false, null,
                enterTapped, false, false, true, additionalPlusOnePlusOneCounters);
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
