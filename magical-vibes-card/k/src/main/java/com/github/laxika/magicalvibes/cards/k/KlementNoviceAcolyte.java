package com.github.laxika.magicalvibes.cards.k;

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
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
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

@CardRegistration(set = "HBG", collectorNumber = "1")
public class KlementNoviceAcolyte extends Card {

    public KlementNoviceAcolyte() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyBoostMatchingHandCardsEffect(
                        new CardTypePredicate(CardType.CREATURE), 1, 1));

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
        effects.add(new SpecializeEffect(color, KlementNoviceAcolyte::specializedFace));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                effects,
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    /** Writes Klement's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setBaseFaceCharacteristics(face, color);
        switch (color) {
            case BLACK -> {
                face.setName("Klement, Death Acolyte");
                face.setManaCost("{1}{W}{B}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                face.setColor(CardColor.WHITE);
                face.setCardText("When this creature specializes, create two 2/2 black Zombie creature tokens.");
                face.setPower(3);
                face.setToughness(3);
            }
            case BLUE -> {
                face.setName("Klement, Knowledge Acolyte");
                face.setManaCost("{1}{W}{U}");
                face.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                face.setColor(CardColor.WHITE);
                face.setKeywords(Set.of(Keyword.VIGILANCE));
                face.setCardText("Vigilance\nWhen this creature specializes, seek three nonland permanent cards. Choose one of those cards and shuffle the rest into your library.");
                face.setPower(3);
                face.setToughness(4);
            }
            case WHITE -> {
                face.setName("Klement, Life Acolyte");
                face.setManaCost("{1}{W}{W}");
                face.setCardText("Lifelink\nWhen this creature specializes, you get a one-time boon with \"When you cast a creature spell, that creature enters with a lifelink counter on it.\"");
                face.setKeywords(Set.of(Keyword.LIFELINK));
                face.setPower(3);
                face.setToughness(3);
            }
            case RED -> {
                face.setName("Klement, Tempest Acolyte");
                face.setManaCost("{1}{R}{W}");
                face.setColors(List.of(CardColor.WHITE, CardColor.RED));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.RED));
                face.setColor(CardColor.WHITE);
                face.setKeywords(Set.of(Keyword.DOUBLE_STRIKE));
                face.setCardText("Double strike\nWhenever Klement, Tempest Acolyte is dealt damage, it deals that much damage to any target.");
                face.setPower(2);
                face.setToughness(3);
                face.addTrigger(Triggers.dealtDamage(),
                        new DealDamageToAnyTargetEffect(new EventValue()));
            }
            case GREEN -> {
                face.setName("Klement, Nature Acolyte");
                face.setManaCost("{1}{G}{W}");
                face.setColors(List.of(CardColor.WHITE, CardColor.GREEN));
                face.setColorIdentity(List.of(CardColor.WHITE, CardColor.GREEN));
                face.setColor(CardColor.WHITE);
                face.setCardText("When Klement, Nature Acolyte leaves the battlefield, create a 4/4 green Ox creature token.");
                face.setPower(4);
                face.setToughness(4);
                face.addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        new CreateTokenEffect(CardType.CREATURE, 1, "Ox", 4, 4, CardColor.GREEN, null,
                                List.of(CardSubtype.OX), Set.of(), Set.of(), false, false, List.of(),
                                List.of(), false, false, false, 0, Set.of()));
            }
            default -> throw new IllegalStateException("Unsupported Klement specialization color: "
                    + color);
        }
        if (color == CardColor.WHITE) {
            return List.of(RegisterDelayedControllerSpellCastTriggerEffect.boon(new CardTypePredicate(CardType.CREATURE),
                            List.of(new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                    CounterType.LIFELINK, 1))));
        } else if (color == CardColor.BLACK) {
            return List.of(CreateTokenEffect.blackZombie(2));
        } else if (color == CardColor.BLUE) {
            // "Seek three nonland permanent cards. Choose one of those cards and shuffle the rest into your library."
            return List.of(SequenceEffect.of(
                    new SeekLibraryEffect(3, new CardAllOfPredicate(List.of(new CardIsPermanentPredicate(),
                            new CardNotPredicate(new CardTypePredicate(CardType.LAND))))),
                    ChooseCardEffect.oneOf(Subjects.cardsThisWay(StepKind.SEEK), "one of the sought cards to keep"),
                    PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.except(
                            Subjects.cardsThisWay(StepKind.SEEK), Subjects.cardsThisWay(StepKind.CHOOSE))),
                    new ShuffleLibraryEffect(false)));
        }
        return List.of();
    }

    private static void setBaseFaceCharacteristics(Card card, CardColor chosenColor) {
        card.setSetCode("HBG");
        card.setCollectorNumber("1");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.TIEFLING, CardSubtype.CLERIC));
        card.setColor(chosenColor);
        card.setColors(List.of(chosenColor));
        card.setColorIdentity(List.of(chosenColor));
        card.setKeywords(Set.of());
    }
}
