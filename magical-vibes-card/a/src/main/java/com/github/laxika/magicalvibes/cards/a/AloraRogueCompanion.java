package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "5")
public class AloraRogueCompanion extends Card {

    public AloraRogueCompanion() {
        addAttackAbility(this, ReturnToHandEffect.target());

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addAttackAbility(Card card, CardEffect delayedReturnEffect) {
        card.target(TargetFilters.attackingCreature(), 0, 1)
                .addTrigger(Triggers.youAttack(),
                        new MakeCreatureUnblockableEffect(),
                        new RegisterDelayedEndStepTriggerEffect(List.of(), delayedReturnEffect));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new DiscardCardTypeCost(discardFilter,
                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()));
        effects.add(new SpecializeEffect(color, AloraRogueCompanion::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                effects,
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedReturnEffect(CardColor color) {
        return switch (color) {
            case WHITE -> SequenceEffect.of(
                    ReturnToHandEffect.target(),
                    new ConditionalEffect(HappenedThisWay.returned(), CreateTokenEffect.whiteSoldier(1)));
            case BLUE -> SequenceEffect.of(
                    ReturnToHandEffect.target(),
                    new ConditionalEffect(HappenedThisWay.returned(), SequenceEffect.of(
                            ChoosePermanentsEffect.exactly(1, Subjects.creaturesYourOpponentsControl(),
                                    "a creature an opponent controls"),
                            new PerpetuallyBoostTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), -1, 0))));
            case BLACK -> SequenceEffect.of(
                    ReturnToHandEffect.target(),
                    new ConditionalEffect(HappenedThisWay.returned(), new LoseLifeEffect(2, Subjects.eachOpponent())));
            case RED -> SequenceEffect.of(
                    ReturnToHandEffect.target(),
                    new ConditionalEffect(HappenedThisWay.returned(), CreateTokenEffect.ofTreasureToken(1)));
            case GREEN -> SequenceEffect.of(
                    ReturnToHandEffect.target(),
                    new ConditionalEffect(HappenedThisWay.returned(),
                            PerpetuallyBoostCardEffect.toCards(Subjects.cardsThisWay(StepKind.RETURN), 1, 1)));
        };
    }

    public static String specializedCardText(CardColor color) {
        String common = "Whenever you attack, up to one target attacking creature can't be blocked this turn. "
                + "At the beginning of the next end step, return that creature to its owner's hand.";
        return switch (color) {
            case WHITE -> common + " If you do, create a 1/1 white Soldier creature token.";
            case BLUE -> common + " If you do, a creature of your choice an opponent controls perpetually gets -1/-0.";
            case BLACK -> common + " If you do, each opponent loses 2 life.";
            case RED -> common + " If you do, create a Treasure token.";
            case GREEN -> common + " If you do, it perpetually gets +1/+1.";
        };
    }

    /** Writes Alora's specialized face for {@code color}; it has no "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setBaseFaceCharacteristics(face);

        switch (color) {
            case WHITE -> {
                face.setName("Alora, Cheerful Mastermind");
                face.setManaCost("{3}{W}{U}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColor(CardColor.WHITE);
                face.setPower(4);
                face.setToughness(4);
            }
            case BLUE -> {
                face.setName("Alora, Cheerful Thief");
                face.setManaCost("{3}{U}{U}");
                face.setPower(4);
                face.setToughness(4);
            }
            case BLACK -> {
                face.setName("Alora, Cheerful Assassin");
                face.setManaCost("{3}{U}{B}");
                face.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                face.setSubtypes(List.of(CardSubtype.HALFLING, CardSubtype.ROGUE,
                        CardSubtype.ASSASSIN));
                face.setColor(CardColor.BLACK);
                face.setPower(4);
                face.setToughness(4);
            }
            case RED -> {
                face.setName("Alora, Cheerful Swashbuckler");
                face.setManaCost("{3}{U}{R}");
                face.setColors(List.of(CardColor.BLUE, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                face.setColor(CardColor.RED);
                face.setPower(4);
                face.setToughness(4);
            }
            case GREEN -> {
                face.setName("Alora, Cheerful Scout");
                face.setManaCost("{3}{G}{U}");
                face.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                face.setColor(CardColor.GREEN);
                face.setSubtypes(List.of(CardSubtype.HALFLING, CardSubtype.ROGUE,
                        CardSubtype.SCOUT));
                face.setPower(4);
                face.setToughness(4);
            }
            default -> throw new IllegalStateException("Unsupported Alora specialization color: "
                    + color);
        }

        face.setCardText(specializedCardText(color));
        addAttackAbility(face, specializedReturnEffect(color));
        return List.of();
    }

    private static void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("5");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HALFLING, CardSubtype.ROGUE));
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setColorIdentity(List.of(CardColor.BLUE));
        card.setKeywords(Set.of());
    }
}
