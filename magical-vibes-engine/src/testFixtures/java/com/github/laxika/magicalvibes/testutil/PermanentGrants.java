package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Gives a permanent a type, colour, keyword, protection, ability, P/T boost or base P/T, or takes a keyword or all its
 * abilities away, as a resolved effect would (CR 611.2a): a floating effect on the permanent at its own timestamp, with
 * no stated duration or until end of turn. Reads back what the floating effects on a permanent give it or take from it, of any duration.
 */
public final class PermanentGrants {

    private PermanentGrants() {
    }

    /** Makes {@code permanent} also {@code subtype}. */
    public static FloatingContinuousEffect subtype(GameData gd, Permanent permanent, CardSubtype subtype) {
        return grant(gd, permanent, new GrantSubtypeEffect(subtype, Subjects.target(), false));
    }

    /** Makes {@code permanent} also {@code color}. */
    public static FloatingContinuousEffect color(GameData gd, Permanent permanent, CardColor color) {
        return grant(gd, permanent, new GrantColorEffect(color, Subjects.target(), false));
    }

    /** Gives {@code permanent} {@code keyword}. */
    public static FloatingContinuousEffect keyword(GameData gd, Permanent permanent, Keyword keyword) {
        return grant(gd, permanent, new GrantKeywordEffect(keyword, Subjects.target(), Duration.PERMANENT));
    }

    /** Gives {@code permanent} {@code keyword} until end of turn. */
    public static FloatingContinuousEffect keywordUntilEndOfTurn(GameData gd, Permanent permanent, Keyword keyword) {
        return untilEndOfTurn(gd, permanent, new GrantKeywordEffect(keyword, Subjects.target()));
    }

    /** Gives {@code permanent} {@code keywords} until end of turn. */
    public static FloatingContinuousEffect keywordsUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                  Set<Keyword> keywords) {
        return untilEndOfTurn(gd, permanent, new GrantKeywordEffect(Set.copyOf(keywords), Subjects.target()));
    }

    /** Makes {@code permanent} lose {@code keyword} until end of turn. */
    public static FloatingContinuousEffect removeKeywordUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                       Keyword keyword) {
        return untilEndOfTurn(gd, permanent, new RemoveKeywordEffect(keyword, Subjects.target()));
    }

    /** Gives {@code permanent} protection from {@code color} until end of turn. */
    public static FloatingContinuousEffect protectionFromColorUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                             CardColor color) {
        return untilEndOfTurn(gd, permanent, new ProtectionFromColorsEffect(Set.of(color), Subjects.target()));
    }

    /** Gives {@code permanent} protection from {@code type} until end of turn. */
    public static FloatingContinuousEffect protectionFromCardTypeUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                                CardType type) {
        return untilEndOfTurn(gd, permanent,
                new GrantEffectEffect(new ProtectionFromCardTypesEffect(Set.of(type)), Subjects.target()));
    }

    /** Makes {@code permanent} also {@code type}. */
    public static FloatingContinuousEffect cardType(GameData gd, Permanent permanent, CardType type) {
        return grant(gd, permanent, new GrantCardTypeEffect(type, Subjects.target()));
    }

    /** Makes {@code permanent} {@code supertype} ({@code gained}) or no longer {@code supertype}. */
    public static FloatingContinuousEffect supertype(GameData gd, Permanent permanent, CardSupertype supertype,
                                                     boolean gained) {
        return grant(gd, permanent, new SetTargetPermanentSupertypeEffect(supertype, gained));
    }

    /** Gives {@code permanent} +{@code power}/+{@code toughness} until end of turn (layer 7c). */
    public static FloatingContinuousEffect boostUntilEndOfTurn(GameData gd, Permanent permanent, int power,
                                                               int toughness) {
        return gd.addPowerToughnessBoost("Test", null, gd.findControllerOf(permanent), permanent.getId(), power,
                toughness, Duration.UNTIL_END_OF_TURN);
    }

    /** The power the floating P/T boosts on {@code permanent} give it, of any duration. */
    public static int powerBoostOf(GameData gd, Permanent permanent) {
        return gd.floatingPowerBoost(permanent.getId());
    }

    /** The toughness the floating P/T boosts on {@code permanent} give it, of any duration. */
    public static int toughnessBoostOf(GameData gd, Permanent permanent) {
        return gd.floatingToughnessBoost(permanent.getId());
    }

    /**
     * Makes the floating P/T boosts on {@code permanent} give it {@code power} power in all, by giving it the
     * difference until end of turn.
     */
    public static void setPowerBoost(GameData gd, Permanent permanent, int power) {
        boostUntilEndOfTurn(gd, permanent, power - powerBoostOf(gd, permanent), 0);
    }

    /**
     * Makes the floating P/T boosts on {@code permanent} give it {@code toughness} toughness in all, by giving it
     * the difference until end of turn.
     */
    public static void setToughnessBoost(GameData gd, Permanent permanent, int toughness) {
        boostUntilEndOfTurn(gd, permanent, 0, toughness - toughnessBoostOf(gd, permanent));
    }

    /** Gives {@code permanent} base power and toughness {@code power}/{@code toughness} until end of turn (layer 7b). */
    public static FloatingContinuousEffect basePowerToughnessUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                            int power, int toughness) {
        return untilEndOfTurn(gd, permanent, new SetBasePowerToughnessEffect(power, toughness));
    }

    /** Gives {@code permanent} base power and toughness {@code power}/{@code toughness} with no duration (layer 7b). */
    public static FloatingContinuousEffect basePowerToughness(GameData gd, Permanent permanent, int power,
                                                              int toughness) {
        return grant(gd, permanent, SetBasePowerToughnessEffect.indefinitely(power, toughness));
    }

    /** Whether a floating effect sets {@code permanent}'s base power or toughness (layer 7b). */
    public static boolean hasBasePowerToughnessSet(GameData gd, Permanent permanent) {
        return gd.hasFloatingEffectOnPermanent(permanent.getId(), SetBasePowerToughnessEffect.class);
    }

    /** The subtypes the floating effects on {@code permanent} add to it, oldest first. */
    public static List<CardSubtype> subtypesOf(GameData gd, Permanent permanent) {
        List<CardSubtype> subtypes = new ArrayList<>();
        for (FloatingContinuousEffect fe : gd.floatingEffectsOnPermanent(permanent.getId(), GrantSubtypeEffect.class)) {
            CardSubtype subtype = ((GrantSubtypeEffect) fe.effect()).subtype();
            if (!subtypes.contains(subtype)) {
                subtypes.add(subtype);
            }
        }
        return subtypes;
    }

    /** The colours the floating effects on {@code permanent} give it. */
    public static Set<CardColor> colorsOf(GameData gd, Permanent permanent) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        gd.floatingEffectsOnPermanent(permanent.getId(), GrantColorEffect.class)
                .forEach(fe -> colors.add(((GrantColorEffect) fe.effect()).color()));
        return colors;
    }

    /** The keywords the floating effects on {@code permanent} give it. */
    public static Set<Keyword> keywordsOf(GameData gd, Permanent permanent) {
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        gd.floatingEffectsOnPermanent(permanent.getId(), GrantKeywordEffect.class)
                .forEach(fe -> keywords.addAll(((GrantKeywordEffect) fe.effect()).keywords()));
        return keywords;
    }

    /** The card types the floating effects on {@code permanent} add to it. */
    public static Set<CardType> cardTypesOf(GameData gd, Permanent permanent) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        gd.floatingEffectsOnPermanent(permanent.getId(), GrantCardTypeEffect.class)
                .forEach(fe -> types.add(((GrantCardTypeEffect) fe.effect()).cardType()));
        return types;
    }

    /** Ends the floating effects that give {@code permanent} {@code keyword}. */
    public static void endKeywordGrants(GameData gd, Permanent permanent, Keyword keyword) {
        gd.floatingEffectsOnPermanent(permanent.getId(), GrantKeywordEffect.class).stream()
                .filter(fe -> ((GrantKeywordEffect) fe.effect()).keywords().contains(keyword))
                .forEach(gd::endFloatingEffect);
    }

    /** Ends the floating effects that make {@code permanent} lose {@code keyword}. */
    public static void endKeywordRemovals(GameData gd, Permanent permanent, Keyword keyword) {
        gd.floatingEffectsOnPermanent(permanent.getId(), RemoveKeywordEffect.class).stream()
                .filter(fe -> ((RemoveKeywordEffect) fe.effect()).keyword() == keyword)
                .forEach(gd::endFloatingEffect);
    }

    /** The keywords the floating effects on {@code permanent} take from it. */
    public static Set<Keyword> removedKeywordsOf(GameData gd, Permanent permanent) {
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        gd.floatingEffectsOnPermanent(permanent.getId(), RemoveKeywordEffect.class)
                .forEach(fe -> keywords.add(((RemoveKeywordEffect) fe.effect()).keyword()));
        return keywords;
    }

    /** The colours the floating effects on {@code permanent} give it protection from. */
    public static Set<CardColor> protectionColorsOf(GameData gd, Permanent permanent) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        protectionGrantsOf(gd, permanent).forEach(protection -> colors.addAll(protection.protectionFromColors()));
        return colors;
    }

    /** The card types the floating effects on {@code permanent} give it protection from. */
    public static Set<CardType> protectionCardTypesOf(GameData gd, Permanent permanent) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        protectionGrantsOf(gd, permanent).forEach(protection -> types.addAll(protection.protectionFromCardTypes()));
        return types;
    }

    /** Whether a floating effect on {@code permanent} gives it protection from colorless. */
    public static boolean hasProtectionFromColorless(GameData gd, Permanent permanent) {
        return protectionGrantsOf(gd, permanent).stream().anyMatch(ProtectionGrantingEffect::protectionFromColorless);
    }

    /** The players the floating effects on {@code permanent} give it protection from. */
    public static Set<UUID> protectionPlayersOf(GameData gd, Permanent permanent) {
        Set<UUID> players = new HashSet<>();
        protectionGrantsOf(gd, permanent).forEach(protection -> players.addAll(protection.protectionFromPlayers()));
        return players;
    }

    private static List<ProtectionGrantingEffect> protectionGrantsOf(GameData gd, Permanent permanent) {
        List<ProtectionGrantingEffect> protections = new ArrayList<>();
        for (FloatingContinuousEffect fe : gd.floatingEffectsOnPermanent(permanent.getId(),
                ProtectionFromColorsEffect.class, ProtectionFromPlayersEffect.class, GrantEffectEffect.class)) {
            CardEffect effect = fe.effect() instanceof GrantEffectEffect granted ? granted.effect() : fe.effect();
            if (effect instanceof ProtectionGrantingEffect protection) {
                protections.add(protection);
            }
        }
        return protections;
    }

    /** Makes {@code permanent} lose all its abilities until end of turn, as Merfolk Trickster does (layer 6). */
    public static FloatingContinuousEffect loseAllAbilitiesUntilEndOfTurn(GameData gd, Permanent permanent) {
        return untilEndOfTurn(gd, permanent, new LosesAllAbilitiesEffect(Subjects.target(), Duration.UNTIL_END_OF_TURN));
    }

    /** Gives {@code permanent} {@code ability} until end of turn (layer 6). */
    public static void abilityUntilEndOfTurn(GameData gd, Permanent permanent, Ability ability) {
        gd.grantAbilityToPermanent("Test", null, gd.findControllerOf(permanent), ability, permanent.getId(),
                Duration.UNTIL_END_OF_TURN);
    }

    /** Gives {@code permanent} {@code ability} with no duration (layer 6). */
    public static void ability(GameData gd, Permanent permanent, Ability ability) {
        gd.grantAbilityToPermanent("Test", null, gd.findControllerOf(permanent), ability, permanent.getId(),
                Duration.PERMANENT);
    }

    /**
     * The effects of the triggered abilities with {@code trigger} that the floating effects on {@code permanent} grant
     * it, of any duration, oldest first.
     */
    public static List<CardEffect> grantedTriggerEffects(GameData gd, Permanent permanent, Trigger trigger) {
        List<CardEffect> effects = new ArrayList<>();
        for (FloatingContinuousEffect fe : gd.floatingEffectsOnPermanent(permanent.getId(),
                GrantTriggeredAbilityEffect.class)) {
            if (((GrantTriggeredAbilityEffect) fe.effect()).grantedAbility() instanceof TriggeredAbility triggered
                    && triggered.hasTrigger(trigger)) {
                effects.addAll(AbilityEffects.effectsOf(triggered));
            }
        }
        return effects;
    }

    /** The triggered abilities the floating effects on {@code permanent} grant it, of any duration, oldest first. */
    public static List<TriggeredAbility> grantedTriggeredAbilities(GameData gd, Permanent permanent) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (FloatingContinuousEffect fe : gd.floatingEffectsOnPermanent(permanent.getId(),
                GrantTriggeredAbilityEffect.class)) {
            if (((GrantTriggeredAbilityEffect) fe.effect()).grantedAbility() instanceof TriggeredAbility triggered) {
                abilities.add(triggered);
            }
        }
        return abilities;
    }

    /** The activated abilities the floating effects on {@code permanent} grant it, of any duration, oldest first. */
    public static List<ActivatedAbility> grantedActivatedAbilities(GameData gd, Permanent permanent) {
        List<ActivatedAbility> abilities = new ArrayList<>();
        for (FloatingContinuousEffect fe : gd.floatingEffectsOnPermanent(permanent.getId(),
                GrantActivatedAbilityEffect.class)) {
            abilities.add(((GrantActivatedAbilityEffect) fe.effect()).ability());
        }
        return abilities;
    }

    private static FloatingContinuousEffect untilEndOfTurn(GameData gd, Permanent permanent, CardEffect effect) {
        return gd.addEffectOnPermanent("Test", null, gd.findControllerOf(permanent), effect, permanent.getId(),
                Duration.UNTIL_END_OF_TURN);
    }

    private static FloatingContinuousEffect grant(GameData gd, Permanent permanent, CardEffect effect) {
        return gd.addEffectOnPermanent("Test", null, gd.findControllerOf(permanent), effect, permanent.getId(),
                Duration.PERMANENT);
    }
}
