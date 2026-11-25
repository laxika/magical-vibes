package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawAndThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "12")
public class AmbergrisCitadelAgent extends Card {

    public AmbergrisCitadelAgent() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new DiscardOwnHandThenDrawEffect(new Fixed(2)),
                "Discard your hand and draw two cards?"));

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
        effects.add(new SpecializeEffect(color, AmbergrisCitadelAgent::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                effects,
                "Specialize {3} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedAttackAbility(CardColor color) {
        int drawCount = color == CardColor.BLUE ? 3 : 2;
        CardEffect discardAndDraw = color == CardColor.BLUE
                ? new DiscardOwnHandThenDrawEffect(new Fixed(drawCount))
                : new DiscardOwnHandThenDrawAndThenEffect(drawCount, specializedAttackRider(color),
                        color == CardColor.BLACK || color == CardColor.GREEN);
        return new MayEffect(discardAndDraw,
                "Discard your hand and draw " + drawCount + " cards?");
    }

    private static CardEffect specializedAttackRider(CardColor color) {
        CardsDiscardedOrCycledThisTurn discardedCount = new CardsDiscardedOrCycledThisTurn();
        return switch (color) {
            case WHITE -> new BoostAllOwnCreaturesEffect(discardedCount, discardedCount,
                    new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
            case BLACK -> new BoostTargetCreatureEffect(new Scaled(discardedCount, -1),
                    new Scaled(discardedCount, -1), TargetFilters.creatureAnOpponentControls().predicate());
            case RED -> new DealDamageToPlayersEffect(discardedCount, Subjects.eachOpponent());
            case GREEN -> new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                    discardedCount, null, anotherCreatureYouControl(), false, null);
            case BLUE -> throw new IllegalArgumentException("Blue Ambergris has no attack rider");
        };
    }

    private static PermanentPredicate anotherCreatureYouControl() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }

    public static void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("12");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.DWARF, CardSubtype.CLERIC));
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setColorIdentity(List.of(CardColor.RED));
        card.setKeywords(Set.of(Keyword.HASTE));
    }

    /** Writes Ambergris's specialized face for {@code color}; it has no "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setBaseFaceCharacteristics(face);
        switch (color) {
            case WHITE -> {
                face.setName("Ambergris, Agent of Law");
                face.setManaCost("{2}{R}{W}");
                face.setColors(List.of(CardColor.RED, CardColor.WHITE));
                face.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                face.setColor(CardColor.WHITE);
                face.setPower(4);
                face.setToughness(3);
                face.setCardText("Haste\nWhenever Ambergris, Agent of Law attacks, you may discard your hand and draw two cards. If you do, other creatures you control get +X/+X until end of turn, where X is the number of cards you've discarded this turn.");
            }
            case BLUE -> {
                face.setName("Ambergris, Agent of Progress");
                face.setManaCost("{2}{U}{R}");
                face.setColors(List.of(CardColor.BLUE, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                face.setColor(CardColor.BLUE);
                face.setPower(4);
                face.setToughness(3);
                face.setCardText("Haste\nWhenever Ambergris, Agent of Progress attacks, you may discard your hand and draw three cards.");
            }
            case BLACK -> {
                face.setName("Ambergris, Agent of Tyranny");
                face.setManaCost("{2}{B}{R}");
                face.setColors(List.of(CardColor.BLACK, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                face.setColor(CardColor.BLACK);
                face.setPower(4);
                face.setToughness(3);
                face.setCardText("Haste\nWhenever Ambergris, Agent of Tyranny attacks, you may discard your hand and draw two cards. When you do, target creature an opponent controls gets -X/-X until end of turn, where X is the number of cards you've discarded this turn.");
            }
            case RED -> {
                face.setName("Ambergris, Agent of Destruction");
                face.setManaCost("{2}{R}{R}");
                face.setColors(List.of(CardColor.RED));
                face.setColorIdentity(List.of(CardColor.RED));
                face.setColor(CardColor.RED);
                face.setPower(4);
                face.setToughness(3);
                face.setCardText("Haste\nWhenever Ambergris, Agent of Destruction attacks, you may discard your hand and draw two cards. If you do, Ambergris deals X damage to each opponent, where X is the number of cards you've discarded this turn.");
            }
            case GREEN -> {
                face.setName("Ambergris, Agent of Balance");
                face.setManaCost("{2}{R}{G}");
                face.setColors(List.of(CardColor.RED, CardColor.GREEN));
                face.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                face.setColor(CardColor.GREEN);
                face.setPower(4);
                face.setToughness(3);
                face.setCardText("Haste\nWhenever Ambergris, Agent of Balance attacks, you may discard your hand and draw two cards. When you do, put X +1/+1 counters on another target creature you control, where X is the number of cards you've discarded this turn.");
            }
            default -> throw new IllegalStateException("Unsupported Ambergris specialization color: "
                    + color);
        }

        face.addTrigger(Triggers.attacks(), specializedAttackAbility(color));
        return List.of();
    }
}
