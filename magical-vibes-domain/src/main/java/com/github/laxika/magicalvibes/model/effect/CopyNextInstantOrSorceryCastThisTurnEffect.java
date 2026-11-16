package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Creates the delayed triggered ability "When you next cast an instant or sorcery spell this turn, copy that spell. You
 * may choose new targets for the copy." ({@link #delayedAbility}): each resolution makes one, which triggers only the
 * next time it applies and ends with the turn if it hasn't (CR 603.7b). Unlike Primal Wellspring's mana-linked copy
 * ({@link AwardAnyColorManaEffect} with {@link ManaSpendRestriction#INSTANT_SORCERY_COPY}), which rides on the produced
 * mana itself, this ability isn't tied to any mana.
 * <p>
 * A non-null {@code maxManaValue} limits it to spells with that mana value or less ("with mana value 2 or less"). A
 * non-null {@code dynamicCopyCount} is how many copies it makes, counted as it resolves ("copy it for each time you've
 * cast your commander from the command zone this game", "copy that spell twice").
 * <p>
 * Used by Chandra, the Firebrand's −2 ability.
 */
public record CopyNextInstantOrSorceryCastThisTurnEffect(
        Integer maxManaValue,
        DynamicAmount dynamicCopyCount
) implements CardEffect {

    public CopyNextInstantOrSorceryCastThisTurnEffect() {
        this(null, null);
    }

    public CopyNextInstantOrSorceryCastThisTurnEffect(Integer maxManaValue) {
        this(maxManaValue, null);
    }

    /** Creates a delayed copy whose number of copies is counted as it resolves. */
    public CopyNextInstantOrSorceryCastThisTurnEffect(DynamicAmount dynamicCopyCount) {
        this(null, dynamicCopyCount);
    }

    /** The delayed triggered ability this creates: "when you next cast an instant or sorcery spell, copy it". */
    public TriggeredAbility delayedAbility() {
        CopyTriggeringSpellEffect copy = new CopyTriggeringSpellEffect();
        return delayedAbility(dynamicCopyCount == null ? copy : copy.withCopies(dynamicCopyCount));
    }

    /** As {@link #delayedAbility()}, making {@code copies} copies ("copy that spell X times", fixed as it is made). */
    public TriggeredAbility delayedAbility(int copies) {
        return delayedAbility(new CopyTriggeringSpellEffect().withCopies(copies));
    }

    private TriggeredAbility delayedAbility(CopyTriggeringSpellEffect copy) {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));
        CardPredicate spell = maxManaValue == null ? instantOrSorcery
                : new CardAllOfPredicate(List.of(instantOrSorcery, new CardMaxManaValuePredicate(maxManaValue)));
        return new TriggeredAbility(Triggers.youCastSpell().withFilter(spell), copy);
    }
}
