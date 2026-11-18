package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Gives a permanent a type, colour, keyword or protection, or takes a keyword away, as a resolved effect would
 * (CR 611.2a): a floating effect on the permanent at its own timestamp, with no stated duration or until end of
 * turn. Reads back what the floating effects on a permanent give it or take from it, of any duration.
 */
public final class PermanentGrants {

    private PermanentGrants() {
    }

    /** Makes {@code permanent} also {@code subtype}. */
    public static FloatingContinuousEffect subtype(GameData gd, Permanent permanent, CardSubtype subtype) {
        return grant(gd, permanent, new GrantSubtypeEffect(subtype, GrantScope.TARGET, false));
    }

    /** Makes {@code permanent} also {@code color}. */
    public static FloatingContinuousEffect color(GameData gd, Permanent permanent, CardColor color) {
        return grant(gd, permanent, new GrantColorEffect(color, GrantScope.TARGET, false));
    }

    /** Gives {@code permanent} {@code keyword}. */
    public static FloatingContinuousEffect keyword(GameData gd, Permanent permanent, Keyword keyword) {
        return grant(gd, permanent, new GrantKeywordEffect(keyword, GrantScope.TARGET, Duration.PERMANENT));
    }

    /** Gives {@code permanent} {@code keyword} until end of turn. */
    public static FloatingContinuousEffect keywordUntilEndOfTurn(GameData gd, Permanent permanent, Keyword keyword) {
        return untilEndOfTurn(gd, permanent, new GrantKeywordEffect(keyword, GrantScope.TARGET));
    }

    /** Gives {@code permanent} {@code keywords} until end of turn. */
    public static FloatingContinuousEffect keywordsUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                  Set<Keyword> keywords) {
        return untilEndOfTurn(gd, permanent, new GrantKeywordEffect(Set.copyOf(keywords), GrantScope.TARGET));
    }

    /** Makes {@code permanent} lose {@code keyword} until end of turn. */
    public static FloatingContinuousEffect removeKeywordUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                       Keyword keyword) {
        return untilEndOfTurn(gd, permanent, new RemoveKeywordEffect(keyword, GrantScope.TARGET));
    }

    /** Gives {@code permanent} protection from {@code color} until end of turn. */
    public static FloatingContinuousEffect protectionFromColorUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                             CardColor color) {
        return untilEndOfTurn(gd, permanent, new ProtectionFromColorsEffect(Set.of(color), GrantScope.TARGET));
    }

    /** Gives {@code permanent} protection from {@code type} until end of turn. */
    public static FloatingContinuousEffect protectionFromCardTypeUntilEndOfTurn(GameData gd, Permanent permanent,
                                                                                CardType type) {
        return untilEndOfTurn(gd, permanent,
                new GrantEffectEffect(new ProtectionFromCardTypesEffect(Set.of(type)), GrantScope.TARGET));
    }

    /** Makes {@code permanent} also {@code type}. */
    public static FloatingContinuousEffect cardType(GameData gd, Permanent permanent, CardType type) {
        return grant(gd, permanent, new GrantCardTypeEffect(type, GrantScope.TARGET));
    }

    /** Makes {@code permanent} {@code supertype} ({@code gained}) or no longer {@code supertype}. */
    public static FloatingContinuousEffect supertype(GameData gd, Permanent permanent, CardSupertype supertype,
                                                     boolean gained) {
        return grant(gd, permanent, new SetTargetPermanentSupertypeEffect(supertype, gained));
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

    private static FloatingContinuousEffect untilEndOfTurn(GameData gd, Permanent permanent, CardEffect effect) {
        return gd.addEffectOnPermanent("Test", null, gd.findControllerOf(permanent), effect, permanent.getId(),
                Duration.UNTIL_END_OF_TURN);
    }

    private static FloatingContinuousEffect grant(GameData gd, Permanent permanent, CardEffect effect) {
        return gd.addEffectOnPermanent("Test", null, gd.findControllerOf(permanent), effect, permanent.getId(),
                Duration.PERMANENT);
    }
}
