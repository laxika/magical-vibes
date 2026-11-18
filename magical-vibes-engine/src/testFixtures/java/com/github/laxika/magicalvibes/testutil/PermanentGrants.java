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
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Gives a permanent a type, colour or keyword "in addition to its others" with no stated duration, as a resolved
 * effect would (CR 611.2a): a floating effect on the permanent at its own timestamp. Reads back what the floating
 * effects on a permanent give it, of any duration, as the fields these grants used to be kept in.
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

    private static FloatingContinuousEffect grant(GameData gd, Permanent permanent, CardEffect effect) {
        return gd.addEffectOnPermanent("Test", null, gd.findControllerOf(permanent), effect, permanent.getId(),
                Duration.PERMANENT);
    }
}
