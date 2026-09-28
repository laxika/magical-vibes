package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeGutEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "13")
public class GutFanaticalPriestess extends Card {

    public GutFanaticalPriestess() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature you control"
        ), 0, 1);
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Second target must be a creature you don't control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                MarkTargetCreatureExileInsteadOfDieThisTurnEffect.withSourceTracking())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new FightTargetsEffect());

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new DiscardCardTypeCost(discardFilter,
                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()));
        effects.add(new SpecializeGutEffect(color));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                effects,
                "Specialize {5} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopyEffect(color));
    }

    private static CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect(CardColor color) {
        return switch (color) {
            case WHITE -> tokenCopyEffect(2, 2, 2, Set.of());
            case BLUE -> tokenCopyEffect(1, 3, 3, Set.of(Keyword.FLYING));
            case BLACK -> tokenCopyEffect(1, 4, 4, Set.of(Keyword.MENACE));
            case RED -> tokenCopyEffect(1, null, null, Set.of(Keyword.DOUBLE_STRIKE));
            case GREEN -> tokenCopyEffect(1, 5, 5, Set.of(Keyword.TRAMPLE));
        };
    }

    private static CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect(
            int amount, Integer power, Integer toughness, Set<Keyword> keywords) {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), power, toughness, Map.of(), true, false, true, false,
                false, false, null, keywords, false, Map.of(), List.of(), false, false,
                new Fixed(amount), false, Set.of(), false);
    }

    public static void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("13");
        card.setSubtypes(List.of(CardSubtype.GOBLIN, CardSubtype.SHAMAN));
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setColorIdentity(List.of(CardColor.RED));
        card.setKeywords(Set.of());
    }

    public static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Gut, Zealous Fanatic");
                card.setManaCost("{4}{R}{R}{W}");
                card.setColors(List.of(CardColor.RED, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setPower(5);
                card.setToughness(4);
                card.setCardText("When this creature specializes, create two tokens that are copies of a creature "
                        + "card exiled with this creature, except they're 2/2 and have haste. Sacrifice them at "
                        + "the beginning of your next end step.");
            }
            case BLUE -> {
                card.setName("Gut, Devious Fanatic");
                card.setManaCost("{4}{U}{R}{R}");
                card.setColors(List.of(CardColor.BLUE, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                card.setColor(CardColor.BLUE);
                card.setPower(5);
                card.setToughness(4);
                card.setCardText("When this creature specializes, create a token that's a copy of a creature card "
                        + "exiled with this creature, except it's 3/3 and has flying and haste. Sacrifice it at "
                        + "the beginning of your next end step.");
            }
            case BLACK -> {
                card.setName("Gut, Brutal Fanatic");
                card.setManaCost("{4}{B}{R}{R}");
                card.setColors(List.of(CardColor.BLACK, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                card.setColor(CardColor.BLACK);
                card.setPower(5);
                card.setToughness(4);
                card.setCardText("When this creature specializes, create a token that's a copy of a creature card "
                        + "exiled with this creature, except it's 4/4 and has menace and haste. Sacrifice it at "
                        + "the beginning of your next end step.");
            }
            case RED -> {
                card.setName("Gut, Furious Fanatic");
                card.setManaCost("{4}{R}{R}{R}");
                card.setColors(List.of(CardColor.RED));
                card.setColorIdentity(List.of(CardColor.RED));
                card.setColor(CardColor.RED);
                card.setPower(5);
                card.setToughness(4);
                card.setCardText("When this creature specializes, create a token that's a copy of a creature card "
                        + "exiled with this creature, except it has double strike and haste. Sacrifice it at the "
                        + "beginning of your next end step.");
            }
            case GREEN -> {
                card.setName("Gut, Bestial Fanatic");
                card.setManaCost("{4}{R}{R}{G}");
                card.setColors(List.of(CardColor.RED, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setPower(5);
                card.setToughness(4);
                card.setCardText("When this creature specializes, create a token that's a copy of a creature card "
                        + "exiled with this creature, except it's 5/5 and has trample and haste. Sacrifice it at "
                        + "the beginning of your next end step.");
            }
            default -> throw new IllegalStateException("Unsupported Gut specialization color: " + color);
        }
    }
}
