package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Static self-effect: "this has all activated abilities of [permanents]". The source has the activated abilities each
 * permanent {@link #permanents()} names has now (its card's, unless it is face down or lost them, and the ones granted
 * to it), never counting itself: Marvin, Murderous Mimic and Robaran Mercenaries ("creatures you control" with a
 * filter), Drana and Linvala ("creatures your opponents control"), Manascape Refractor ("all lands on the
 * battlefield"), Experiment Kraj ("each other creature with a +1/+1 counter on it"), Scheming Fence ("the chosen
 * permanent except for loyalty abilities") and Nicol Bolas, Dragon-God ("all loyalty abilities of all other
 * planeswalkers"). Only activated abilities are gained, never triggered or static ones (the cards' rulings).
 *
 * @param permanents which permanents lend their abilities, a static set judged by the layered pass
 * @param filter     a further restriction on those permanents, judged for the source ({@code null} for none)
 * @param abilities  which of their activated abilities are gained
 */
public record GainActivatedAbilitiesOfOwnCreaturesEffect(PermanentSubject permanents, PermanentPredicate filter,
                                                         Abilities abilities)
        implements CardEffect {

    /** Which activated abilities of the lending permanents the source has. */
    public enum Abilities {
        /** "All activated abilities". */
        ALL,
        /** "All activated abilities … except for loyalty abilities" (Scheming Fence). */
        NON_LOYALTY,
        /** "All loyalty abilities" (Nicol Bolas, Dragon-God). */
        LOYALTY;

        /** Whether {@code ability} is one of the kind gained. */
        public boolean includes(ActivatedAbility ability) {
            return switch (this) {
                case ALL -> true;
                case NON_LOYALTY -> ability.getLoyaltyCost() == null;
                case LOYALTY -> ability.getLoyaltyCost() != null;
            };
        }
    }

    public GainActivatedAbilitiesOfOwnCreaturesEffect {
        Objects.requireNonNull(permanents, "permanents");
        Objects.requireNonNull(abilities, "abilities");
        if (permanents.needsStackEntry()) {
            throw new IllegalArgumentException("A static ability names a static set: " + permanents);
        }
    }

    /** "All activated abilities of all [filter] creatures you control". */
    public GainActivatedAbilitiesOfOwnCreaturesEffect(PermanentPredicate filter) {
        this(Subjects.creaturesYouControl(), filter, Abilities.ALL);
    }

    /** "All activated abilities of [permanents]". */
    public static GainActivatedAbilitiesOfOwnCreaturesEffect of(PermanentSubject permanents) {
        return new GainActivatedAbilitiesOfOwnCreaturesEffect(permanents, null, Abilities.ALL);
    }

    /** "All activated abilities of [permanents] that match {@code filter}". */
    public static GainActivatedAbilitiesOfOwnCreaturesEffect of(PermanentSubject permanents,
                                                               PermanentPredicate filter) {
        return new GainActivatedAbilitiesOfOwnCreaturesEffect(permanents, filter, Abilities.ALL);
    }

    /** "All loyalty abilities of all other planeswalkers on the battlefield" (Nicol Bolas, Dragon-God). */
    public static GainActivatedAbilitiesOfOwnCreaturesEffect loyaltyAbilitiesOfOtherPlaneswalkers() {
        return new GainActivatedAbilitiesOfOwnCreaturesEffect(
                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsPlaneswalkerPredicate()), null,
                Abilities.LOYALTY);
    }

    /** These abilities, without loyalty abilities ("except for loyalty abilities"). */
    public GainActivatedAbilitiesOfOwnCreaturesEffect exceptLoyaltyAbilities() {
        return new GainActivatedAbilitiesOfOwnCreaturesEffect(permanents, filter, Abilities.NON_LOYALTY);
    }
}
