package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeWilsonEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "19")
public class WilsonBearComrade extends Card {

    public WilsonBearComrade() {
        addWardAbility(this);

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addWardAbility(Card card) {
        card.addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(2));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeWilsonEffect(color)),
                "Specialize {4} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static ActivatedAbility graveyardAbility(CardColor color) {
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new ExileSelfFromGraveyardCost());
        if (color == CardColor.GREEN) {
            effects.add(new PerpetuallyBoostTargetCreatureEffect(1, 1));
        }
        effects.add(graveyardEffect(color));
        return new ActivatedAbility(
                false,
                graveyardManaCost(color),
                effects,
                graveyardAbilityDescription(color),
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED);
    }

    private static String graveyardManaCost(CardColor color) {
        return "{1}{G}{" + switch (color) {
            case WHITE -> "W";
            case BLUE -> "U";
            case BLACK -> "B";
            case RED -> "R";
            case GREEN -> "G";
        } + "}";
    }

    private static String graveyardAbilityDescription(CardColor color) {
        return graveyardManaCost(color) + ", Exile Wilson from your graveyard: Target creature you control "
                + switch (color) {
            case WHITE -> "perpetually gains lifelink.";
            case BLUE -> "perpetually gains \"This creature can't be blocked.\"";
            case BLACK -> "perpetually gains menace.";
            case RED -> "perpetually gains double strike.";
            case GREEN -> "perpetually gets +1/+1 and gains reach, trample, and ward {2}.";
        } + " Activate only as a sorcery.";
    }

    private static CardEffect graveyardEffect(CardColor color) {
        return switch (color) {
            case WHITE -> new PerpetuallyGrantKeywordsToTargetCreatureEffect(Set.of(Keyword.LIFELINK));
            case BLUE -> new PerpetuallyGrantStaticEffectToTargetCreatureEffect(new CantBeBlockedEffect());
            case BLACK -> new PerpetuallyGrantKeywordsToTargetCreatureEffect(Set.of(Keyword.MENACE));
            case RED -> new PerpetuallyGrantKeywordsToTargetCreatureEffect(Set.of(Keyword.DOUBLE_STRIKE));
            case GREEN -> new PerpetuallyGrantKeywordsToTargetCreatureEffect(
                    EnumSet.of(Keyword.REACH, Keyword.TRAMPLE, Keyword.WARD),
                    EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                    new CounterUnlessPaysEffect(2));
        };
    }

    public static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Wilson, Urbane Bear");
                card.setManaCost("{1}{G}{W}");
                card.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setKeywords(EnumSet.of(Keyword.LIFELINK, Keyword.REACH, Keyword.TRAMPLE, Keyword.WARD));
                card.setPower(3);
                card.setToughness(4);
                card.setCardText("Reach, trample, lifelink\nWard {2}\n"
                        + "{1}{G}{W}, Exile Wilson, Urbane Bear from your graveyard: Target creature you control perpetually gains lifelink. Activate only as a sorcery.");
            }
            case BLUE -> {
                card.setName("Wilson, Subtle Bear");
                card.setManaCost("{1}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setKeywords(EnumSet.of(Keyword.REACH, Keyword.TRAMPLE, Keyword.WARD));
                card.setPower(3);
                card.setToughness(3);
                card.setCardText("Reach, trample\nWard {2}\nWilson, Subtle Bear can't be blocked.\n"
                        + "{1}{G}{U}, Exile Wilson from your graveyard: Target creature you control perpetually gains \"This creature can't be blocked.\" Activate only as a sorcery.");
            }
            case BLACK -> {
                card.setName("Wilson, Fearsome Bear");
                card.setManaCost("{1}{B}{G}");
                card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                card.setColor(CardColor.BLACK);
                card.setKeywords(EnumSet.of(Keyword.MENACE, Keyword.REACH, Keyword.TRAMPLE, Keyword.WARD));
                card.setPower(4);
                card.setToughness(4);
                card.setCardText("Menace, reach, trample\nWard {2}\n"
                        + "{1}{B}{G}, Exile Wilson, Fearsome Bear from your graveyard: Target creature you control perpetually gains menace. Activate only as a sorcery.");
            }
            case RED -> {
                card.setName("Wilson, Ardent Bear");
                card.setManaCost("{1}{R}{G}");
                card.setColors(List.of(CardColor.RED, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                card.setColor(CardColor.RED);
                card.setKeywords(EnumSet.of(Keyword.DOUBLE_STRIKE, Keyword.REACH, Keyword.TRAMPLE, Keyword.WARD));
                card.setPower(2);
                card.setToughness(3);
                card.setCardText("Double strike, reach, trample\nWard {2}\n"
                        + "{1}{R}{G}, Exile Wilson, Ardent Bear from your graveyard: Target creature you control perpetually gains double strike. Activate only as a sorcery.");
            }
            case GREEN -> {
                card.setName("Wilson, Majestic Bear");
                card.setManaCost("{1}{G}{G}");
                card.setColors(List.of(CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setKeywords(EnumSet.of(Keyword.REACH, Keyword.TRAMPLE, Keyword.WARD));
                card.setPower(5);
                card.setToughness(5);
                card.setCardText("Reach, trample\nWard {2}\n"
                        + "{1}{G}{G}, Exile Wilson from your graveyard: Target creature you control perpetually gets +1/+1 and gains reach, trample, and ward {2}. Activate only as a sorcery.");
            }
        }
    }
}
