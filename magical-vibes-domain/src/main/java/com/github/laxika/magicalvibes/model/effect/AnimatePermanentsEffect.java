package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

/**
 * Makes one or more permanents become creatures with the given power and toughness.
 * Parameterised by {@link PermanentSubject} (which permanents), {@link Duration} (how long),
 * and the granted subtypes/keywords/colour/card-types.
 *
 * <p>{@code becomesCreature} is {@code false} only for {@link #vehicles}, which sets the
 * Vehicle's base power and toughness and leaves its card types alone.
 *
 * <p>Scopes used by this effect:
 * <ul>
 *   <li>{@link Subjects#source()} — the source permanent (manlands, Crew, Chimeric Staff/Mass,
 *       Warden of the Wall). Until end of turn. A {@code null} power/toughness means "use the
 *       source's printed power/toughness" (Crew on Vehicles).</li>
 *   <li>{@link Subjects#target()} — a target permanent, with {@link Duration#PERMANENT}
 *       (Tezzeret, Waker of the Wilds), {@link Duration#WHILE_SOURCE_ON_BATTLEFIELD}
 *       (Awakener Druid), or {@link Duration#WHILE_SOURCE_REMAINS_TAPPED} (The Blackstaff
 *       of Waterdeep).</li>
 *   <li>{@link Subjects#attached()} — the permanent the source Aura is attached to,
 *       re-derived at resolution and without targeting (the Genju cycle).</li>
 *   <li>{@link Subjects#landsYouControl()} — all lands you control (Sylvan Awakening) until end of turn
 *       or until your next turn.</li>
 *   <li>{@link Subjects#landsTargetPlayerControls()} — all lands the targeted player controls until
 *       end of turn.</li>
 *   <li>{@link Subjects#permanentsYouControl()} — all permanents you control matching {@link #filter}
 *       (The Antiquities War III) until end of turn.</li>
 *   <li>{@link Subjects#createdThisResolution()} — the tokens created earlier in the same
 *       resolution, with {@link Duration#PERMANENT} ("create a Lander token. It becomes a 2/1
 *       artifact creature and gains flying" — Vv'viza, Orbital Overseer).</li>
 * </ul>
 */
public record AnimatePermanentsEffect(DynamicAmount power, DynamicAmount toughness,
                                      List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords,
                                      CardColor animatedColor, Set<CardType> grantedCardTypes,
                                      PermanentSubject subject, Duration duration,
                                      PermanentPredicate filter, Set<CardColor> animatedColors,
                                      boolean cardTypeOverriding,
                                      boolean dynamicPowerToughness,
                                      boolean becomesCreature) implements CardEffect {

    public AnimatePermanentsEffect {
        animatedColors = Set.copyOf(animatedColors);
    }

    /** Every form that makes the permanents creatures; see {@link #vehicles} for the other. */
    public AnimatePermanentsEffect(DynamicAmount power, DynamicAmount toughness,
                                   List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords,
                                   CardColor animatedColor, Set<CardType> grantedCardTypes,
                                   PermanentSubject subject, Duration duration,
                                   PermanentPredicate filter, Set<CardColor> animatedColors,
                                   boolean cardTypeOverriding, boolean dynamicPowerToughness) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, grantedCardTypes,
                subject, duration, filter, animatedColors, cardTypeOverriding, dynamicPowerToughness, true);
    }

    /**
     * Until end of turn, each permanent you control matching {@code filter} becomes a
     * {@code power}/{@code toughness} Vehicle in addition to its other types and gains the keywords,
     * without becoming a creature (Ghirapur). A Vehicle has that power and toughness only once it
     * is also a creature, e.g. after it's crewed (CR 301.7a, 301.7b).
     */
    public static AnimatePermanentsEffect vehicles(int power, int toughness, Set<Keyword> grantedKeywords,
                                                   PermanentPredicate filter) {
        return new AnimatePermanentsEffect(new Fixed(power), new Fixed(toughness), List.of(CardSubtype.VEHICLE),
                grantedKeywords, null, Set.of(), Subjects.permanentsYouControl(), Duration.UNTIL_END_OF_TURN,
                filter, Set.of(), false, false, false);
    }

    public AnimatePermanentsEffect(DynamicAmount power, DynamicAmount toughness,
                                   List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords,
                                   CardColor animatedColor, Set<CardType> grantedCardTypes,
                                   PermanentSubject subject, Duration duration,
                                   PermanentPredicate filter, Set<CardColor> animatedColors) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, grantedCardTypes,
                subject, duration, filter, animatedColors, false, false);
    }

    public AnimatePermanentsEffect(DynamicAmount power, DynamicAmount toughness,
                                   List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords,
                                   CardColor animatedColor, Set<CardType> grantedCardTypes,
                                   PermanentSubject subject, Duration duration,
                                   PermanentPredicate filter) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, grantedCardTypes,
                subject, duration, filter, Set.of(), false, false);
    }

    /**
     * Fully-specified animation with an optional card-type replacement.
     *
     * <p>Most animations add the creature type to the permanent's existing card types. Some
     * effects instead say that the permanent becomes a creature, replacing its other card types.
     */
    public AnimatePermanentsEffect(DynamicAmount power, DynamicAmount toughness,
                                   List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords,
                                   CardColor animatedColor, Set<CardType> grantedCardTypes,
                                   PermanentSubject subject, Duration duration,
                                   PermanentPredicate filter, boolean cardTypeOverriding) {
        this(power, toughness, grantedSubtypes, grantedKeywords, animatedColor, grantedCardTypes,
                subject, duration, filter, Set.of(), cardTypeOverriding, false);
    }

    /** Self-targeting, until end of turn (manlands). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords, CardColor animatedColor) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, animatedColor,
                Set.of(), Subjects.source(), Duration.UNTIL_END_OF_TURN, null, Set.of(), false, false);
    }

    /** Self-targeting, until end of turn, with granted card types (e.g. Inkmoth Nexus). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords, CardColor animatedColor,
                                   Set<CardType> grantedCardTypes) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, animatedColor,
                grantedCardTypes, Subjects.source(), Duration.UNTIL_END_OF_TURN, null, Set.of(), false, false);
    }

    /** Self-targeting, until end of turn, no colour (e.g. Warden of the Wall, Rusted Relic, Glint Hawk Idol). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, null,
                Set.of(), Subjects.source(), Duration.UNTIL_END_OF_TURN, null, Set.of(), false, false);
    }

    /** Fully-specified fixed-P/T form (Sylvan Awakening own-lands, Awakener Druid while-source, Tezzeret/Waker target). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords, CardColor animatedColor,
                                   Set<CardType> grantedCardTypes, PermanentSubject subject, Duration duration) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, animatedColor,
                grantedCardTypes, subject, duration, null, Set.of(), false, false);
    }

    /**
     * Crew: the source Vehicle becomes an artifact creature with its printed power and toughness
     * (the {@code null} amounts) until end of turn. The artifact type is granted explicitly so a
     * Vehicle that lost it earlier this turn is an artifact again once crewed.
     */
    public static AnimatePermanentsEffect crew() {
        return new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null,
                Set.of(CardType.CREATURE, CardType.ARTIFACT), Subjects.source(), Duration.UNTIL_END_OF_TURN, null, Set.of(), false, false);
    }

    /** Self-targeting animation with a fixed set of colors. */
    public static AnimatePermanentsEffect withAnimatedColors(int power, int toughness,
                                                               List<CardSubtype> grantedSubtypes,
                                                               Set<Keyword> grantedKeywords,
                                                               Set<CardColor> animatedColors) {
        return new AnimatePermanentsEffect(new Fixed(power), new Fixed(toughness), grantedSubtypes,
                grantedKeywords, null, Set.of(), Subjects.source(), Duration.UNTIL_END_OF_TURN,
                null, animatedColors, false, false);
    }

    /** Self-targeting animation whose base power and toughness continuously follow the amounts. */
    public static AnimatePermanentsEffect withDynamicPowerToughness(
            DynamicAmount power, DynamicAmount toughness, List<CardSubtype> grantedSubtypes,
            Set<Keyword> grantedKeywords, Set<CardColor> animatedColors) {
        return new AnimatePermanentsEffect(power, toughness, grantedSubtypes, grantedKeywords,
                null, Set.of(), Subjects.source(), Duration.UNTIL_END_OF_TURN,
                null, animatedColors, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            case PermanentSubject.ControlledBy set when set.declaresPlayerTarget() ->
                    TargetSpec.benign(TargetPredicates.player());
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }

    /**
     * The ENCHANTED_PERMANENT subject animates the Aura's attached permanent, so an activation whose
     * source is that Aura captures the attached permanent at activation time (before the Aura can
     * leave the battlefield in response).
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return Subjects.attached().equals(subject);
    }
}
