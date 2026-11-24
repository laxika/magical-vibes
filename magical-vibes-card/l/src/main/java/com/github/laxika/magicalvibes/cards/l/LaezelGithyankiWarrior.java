package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "2")
public class LaezelGithyankiWarrior extends Card {

    public LaezelGithyankiWarrior() {
        addTrigger(Triggers.youCastThisSpell(),
                new GrantTriggeredAbilityToCastSpellEffect(
                        new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                                new MayEffect(
                                        new FlickerEffect(Subjects.source(), null, ReturnTiming.IMMEDIATE,
                                                TurnStep.END_STEP, false, null, null, 0, false, false),
                                        "Exile this creature, then return it to the battlefield under its owner's control?"))));

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
        effects.add(new SpecializeEffect(color, LaezelGithyankiWarrior::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                effects,
                "Specialize {1} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return switch (color) {
            case WHITE -> new SeekLibraryEffect(
                    new Fixed(1),
                    new CardAllOfPredicate(List.of(
                            new CardIsPermanentPredicate(),
                            new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                    LibrarySearchDestination.HAND,
                    new ManaValueBound(new Fixed(3), false, 0));
            case BLUE -> SequenceEffect.of(
                    ChooseCardEffect.atRandom(Subjects.cardsInLibraries(Subjects.eachOpponent(),
                            new CardTypePredicate(CardType.CREATURE)), "a creature card"),
                    ConjureCardsEffect.duplicateOf(Subjects.cardsThisWay(StepKind.CHOOSE),
                            ConjureDestination.intoHand()),
                    new PerpetuallyGrantAnyColorManaToCastEffect(Subjects.cardsThisWay(StepKind.CONJURE)));
            case BLACK -> new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                    new CardTypePredicate(CardType.CREATURE), 2, 3);
            case RED -> CreateTokenEffect.whiteSoldier(2);
            case GREEN -> SequenceEffect.of(
                    new PerpetuallyBoostTargetCreatureEffect(Subjects.otherCreaturesYouControl(), 1, 1),
                    PerpetuallyBoostCardEffect.toCards(
                            Subjects.cardsInYourHand(new CardTypePredicate(CardType.CREATURE)), 1, 1));
        };
    }

    /** Writes Lae'zel's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setBaseFaceCharacteristics(face);

        CardEffect specializedTrigger = specializedTrigger(color);
        switch (color) {
            case WHITE -> {
                face.setName("Lae'zel, Blessed Warrior");
                face.setManaCost("{3}{W}{W}");
                face.setCardText("Double strike\nWhen this creature enters or specializes, seek a nonland permanent card with mana value 3 or less.");
                face.setPower(3);
                face.setToughness(6);
            }
            case BLUE -> {
                face.setName("Lae'zel, Illithid Thrall");
                face.setManaCost("{3}{W}{U}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setSubtypes(List.of(CardSubtype.GITH, CardSubtype.HORROR, CardSubtype.WARRIOR));
                face.setCardText("Double strike\nWhen this creature enters or specializes, conjure a duplicate of a random creature card from an opponent's library into your hand. It perpetually gains \"You may spend mana as though it were mana of any color to cast this spell.\"");
                face.setPower(3);
                face.setToughness(6);
            }
            case BLACK -> {
                face.setName("Lae'zel, Callous Warrior");
                face.setManaCost("{3}{W}{B}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setCardText("Double strike\nWhen this creature enters or specializes, return up to two target creature cards with total mana value 3 or less from your graveyard to the battlefield.");
                face.setPower(3);
                face.setToughness(6);
            }
            case RED -> {
                face.setName("Lae'zel, Wrathful Warrior");
                face.setManaCost("{3}{R}{W}");
                face.setColors(List.of(CardColor.RED, CardColor.WHITE));
                face.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                face.setColor(CardColor.RED);
                face.setCardText("Double strike\nWhen this creature enters or specializes, create two 1/1 white Soldier creature tokens.");
                face.setPower(3);
                face.setToughness(6);
            }
            case GREEN -> {
                face.setName("Lae'zel, Primal Warrior");
                face.setManaCost("{3}{G}{W}");
                face.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                face.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                face.setColor(CardColor.GREEN);
                face.setCardText("Double strike\nWhen this creature enters or specializes, other creatures you control and creature cards in your hand perpetually get +1/+1.");
                face.setPower(3);
                face.setToughness(6);
            }
            default -> throw new IllegalStateException("Unsupported Lae'zel specialization color: "
                    + color);
        }

        face.addTrigger(Triggers.enters(ObjectRelation.THIS), specializedTrigger);
        return List.of(specializedTrigger);
    }

    private static void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("2");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.GITH, CardSubtype.WARRIOR));
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE));
        card.setColorIdentity(List.of(CardColor.WHITE));
        card.setKeywords(Set.of(Keyword.DOUBLE_STRIKE));
    }
}
