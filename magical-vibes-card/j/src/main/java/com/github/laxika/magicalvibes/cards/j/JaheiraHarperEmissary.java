package com.github.laxika.magicalvibes.cards.j;

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
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "16")
public class JaheiraHarperEmissary extends Card {

    private static final Set<CardType> PROTECTED_CARD_TYPES = Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT);

    public JaheiraHarperEmissary() {
        removeKeyword(Keyword.HEXPROOF);
        addHexproofFromArtifactsAndEnchantments(this);

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addHexproofFromArtifactsAndEnchantments(Card card) {
        card.addStaticEffect(TargetingRestrictionEffect.hexproofFromCardTypes(PROTECTED_CARD_TYPES));
        card.addStaticEffect(new TargetingRestrictionEffect(TargetingSourceKind.ABILITIES, true, Set.of(),
                        TargetColorMode.ANY, PROTECTED_CARD_TYPES));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeEffect(color, JaheiraHarperEmissary::specializedFace)),
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static List<CardEffect> specializedTrigger(CardColor color) {
        // The white face's two target groups are read by position; another face's one target is the ability's.
        CardEffect destroy = new DestroyTargetPermanentEffect(false, null, color == CardColor.WHITE ? 0 : -1,
                artifactOrEnchantment());
        CardEffect rider = switch (color) {
            case WHITE -> new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE);
            case BLUE -> new ScryEffect(2);
            case BLACK -> new LoseLifeEffect(3, Subjects.eachOpponent());
            case RED -> RegisterDelayedControllerSpellCastTriggerEffect.boon(
                    new CardTypePredicate(CardType.CREATURE),
                    List.of(PerpetuallyBoostCardEffect.toCards(Subjects.cardsOf(Subjects.triggeringSpell()), 1, 0),
                            PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(Subjects.cardsOf(Subjects.triggeringSpell()),
                                    Keyword.HASTE)));
            case GREEN -> new GainLifeEffect(4);
        };
        return List.of(destroy, rider);
    }

    public static PermanentPredicate artifactOrEnchantment() {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(), new PermanentIsEnchantmentPredicate()));
    }

    public static PermanentPredicate otherCreature() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }

    public static void installSpecializedTargets(Card card, List<CardEffect> effects, CardColor color) {
        card.target(new PermanentPredicateTargetFilter(artifactOrEnchantment(),
                        "Target must be an artifact or enchantment"), 0, 1);
        card.registerEffectTargetIndex(effects.getFirst(), 0);
        if (color == CardColor.WHITE) {
            card.target(new PermanentPredicateTargetFilter(otherCreature(),
                            "Target must be another creature"), 0, 2);
            card.registerEffectTargetIndex(effects.get(1), 1);
        }
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("16");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.ELF, CardSubtype.DRUID));
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setColorIdentity(List.of(CardColor.GREEN));
        card.setKeywords(Set.of());
    }

    /** Writes Jaheira's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        addHexproofFromArtifactsAndEnchantments(face);
        face.setPower(3);
        face.setToughness(4);
        List<CardEffect> specializedTrigger = specializedTrigger(color);
        setFaceCharacteristics(face, color);
        installSpecializedTargets(face, specializedTrigger, color);
        return specializedTrigger;
    }

    private static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Jaheira, Heroic Harper");
                card.setManaCost("{1}{G}{W}");
                card.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "Put a +1/+1 counter on each of up to two other target creatures.");
            }
            case BLUE -> {
                card.setName("Jaheira, Insightful Harper");
                card.setManaCost("{1}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "Scry 2.");
            }
            case BLACK -> {
                card.setName("Jaheira, Ruthless Harper");
                card.setManaCost("{1}{G}{B}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColor(CardColor.BLACK);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "Each opponent loses 3 life.");
            }
            case RED -> {
                card.setName("Jaheira, Stirring Harper");
                card.setManaCost("{1}{G}{R}");
                card.setColors(List.of(CardColor.GREEN, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.RED));
                card.setColor(CardColor.RED);
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "You get a one-time boon with \"When you cast a creature spell, it perpetually gets "
                        + "+1/+0 and gains haste.\"");
            }
            case GREEN -> {
                card.setName("Jaheira, Merciful Harper");
                card.setManaCost("{1}{G}{G}");
                card.setCardText("Hexproof from artifacts and enchantments\n"
                        + "When this creature specializes, destroy up to one target artifact or enchantment. "
                        + "You gain 4 life.");
            }
            default -> throw new IllegalStateException("Unsupported Jaheira specialization color: " + color);
        }
    }
}
