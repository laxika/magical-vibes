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
 * Parameterised by {@link GrantScope} (which permanents), {@link EffectDuration} (how long),
 * and the granted subtypes/keywords/colour/card-types.
 *
 * <p>Scopes used by this effect:
 * <ul>
 *   <li>{@link GrantScope#SELF} — the source permanent (manlands, Crew, Chimeric Staff/Mass,
 *       Warden of the Wall). Until end of turn. A {@code null} power/toughness means "use the
 *       source's printed power/toughness" (Crew on Vehicles).</li>
 *   <li>{@link GrantScope#TARGET} — a target permanent, with {@link EffectDuration#PERMANENT}
 *       (Tezzeret, Waker of the Wilds) or {@link EffectDuration#WHILE_SOURCE_ON_BATTLEFIELD}
 *       (Awakener Druid).</li>
 *   <li>{@link GrantScope#OWN_LANDS} — all lands you control (Sylvan Awakening) until end of turn
 *       or until your next turn.</li>
 *   <li>{@link GrantScope#OWN_PERMANENTS} — all permanents you control matching {@link #filter}
 *       (The Antiquities War III) until end of turn.</li>
 * </ul>
 */
public record AnimatePermanentsEffect(DynamicAmount power, DynamicAmount toughness,
                                      List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords,
                                      CardColor animatedColor, Set<CardType> grantedCardTypes,
                                      GrantScope scope, EffectDuration duration,
                                      PermanentPredicate filter) implements CardEffect {

    /** Self-targeting, until end of turn (manlands). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords, CardColor animatedColor) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, animatedColor,
                Set.of(), GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null);
    }

    /** Self-targeting, until end of turn, with granted card types (e.g. Inkmoth Nexus). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords, CardColor animatedColor,
                                   Set<CardType> grantedCardTypes) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, animatedColor,
                grantedCardTypes, GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null);
    }

    /** Self-targeting, until end of turn, no colour (e.g. Warden of the Wall, Rusted Relic, Glint Hawk Idol). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, null,
                Set.of(), GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null);
    }

    /** Fully-specified fixed-P/T form (Sylvan Awakening own-lands, Awakener Druid while-source, Tezzeret/Waker target). */
    public AnimatePermanentsEffect(int power, int toughness, List<CardSubtype> grantedSubtypes,
                                   Set<Keyword> grantedKeywords, CardColor animatedColor,
                                   Set<CardType> grantedCardTypes, GrantScope scope, EffectDuration duration) {
        this(new Fixed(power), new Fixed(toughness), grantedSubtypes, grantedKeywords, animatedColor,
                grantedCardTypes, scope, duration, null);
    }

    /**
     * Crew: the source Vehicle becomes an artifact creature with its printed power and toughness
     * (the {@code null} amounts) until end of turn.
     */
    public static AnimatePermanentsEffect crew() {
        return new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null,
                Set.of(CardType.CREATURE), GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetCategory.PERMANENT);
            case SELF -> new TargetSpec(TargetCategory.NONE, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
