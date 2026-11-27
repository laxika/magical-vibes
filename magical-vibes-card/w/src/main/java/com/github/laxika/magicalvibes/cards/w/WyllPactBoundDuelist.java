package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "15")
public class WyllPactBoundDuelist extends Card {

    public WyllPactBoundDuelist() {
        PermanentPredicate target = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentMaxManaValuePredicate(4)));
        target(new PermanentPredicateTargetFilter(target,
                "Target must be an artifact or creature an opponent controls with mana value 4 or less"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new com.github.laxika.magicalvibes.model.filter.CardColorPredicate(color),
                new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DiscardCardTypeCost(discardFilter,
                        color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeEffect(color, WyllPactBoundDuelist::specializedFace)),
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return new MayEffect(
                new SacrificePermanentThenEffect(
                        anotherCreatureOrArtifact(), specializedThenEffect(color),
                        "another creature or an artifact", false,
                        color != CardColor.RED && color != CardColor.GREEN),
                "Sacrifice another creature or an artifact?");
    }

    private static CardEffect specializedThenEffect(CardColor color) {
        return switch (color) {
            case WHITE -> ReturnTargetCardsFromGraveyardToBattlefieldEffect
                    .fromControllerGraveyardWithHasteAndConditionalSacrifice(
                            new CardTypePredicate(CardType.CREATURE), 4);
            case BLUE -> ReturnTargetCardsFromGraveyardToHandEffect.exactlyOneForTriggeredAbility(
                    new CardAnyOfPredicate(List.of(
                            new CardTypePredicate(CardType.INSTANT),
                            new CardTypePredicate(CardType.SORCERY))));
            case BLACK -> UnlessPaysEffect.unlessPaysLife(Subjects.targetPlayer(), 5, new DrawCardEffect(3))
                    .withPrompt("Pay 5 life to prevent drawing?");
            case RED -> SequenceEffect.of(
                    new UntapPermanentsEffect(Subjects.source()),
                    new AdditionalCombatMainPhaseEffect(1));
            case GREEN -> SequenceEffect.of(
                    PerpetuallyBoostCardEffect.toCards(Subjects.sourceObjectCard(), 3, 3),
                    PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(Subjects.sourceObjectCard(), Keyword.TRAMPLE));
        };
    }

    public static TargetFilter specializedTargetFilter(CardColor color) {
        return color == CardColor.BLACK
                ? new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent")
                : null;
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("15");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WARLOCK));
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setColorIdentity(List.of(CardColor.RED));
        card.setKeywords(Set.of());
    }

    private static PermanentPredicate anotherCreatureOrArtifact() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                        new PermanentIsArtifactPredicate()))));
    }

    /** Writes Wyll's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        face.setPower(5);
        face.setToughness(5);
        setFaceCharacteristics(face, color);
        TargetFilter targetFilter = specializedTargetFilter(color);
        if (targetFilter != null) {
            face.target(targetFilter);
        }
        return List.of(specializedTrigger(color));
    }

    private static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Wyll of the Celestial Pact");
                card.setManaCost("{3}{R}{R}{W}");
                card.setColors(List.of(CardColor.RED, CardColor.WHITE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                card.setColor(CardColor.WHITE);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, return target creature card from your graveyard to the battlefield. It gains "
                        + "haste. If its mana value is 4 or greater, sacrifice it at the beginning of your next end step.");
            }
            case BLUE -> {
                card.setName("Wyll of the Elder Pact");
                card.setManaCost("{3}{R}{R}{U}");
                card.setColors(List.of(CardColor.RED, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.BLUE));
                card.setColor(CardColor.BLUE);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, return target instant or sorcery card from your graveyard to your hand.");
            }
            case BLACK -> {
                card.setName("Wyll of the Fiend Pact");
                card.setManaCost("{3}{B}{R}{R}");
                card.setColors(List.of(CardColor.BLACK, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                card.setColor(CardColor.BLACK);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "When you do, draw three cards unless target opponent pays 5 life.");
            }
            case RED -> {
                card.setName("Wyll of the Blade Pact");
                card.setManaCost("{3}{R}{R}{R}");
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "If you do, untap Wyll of the Blade Pact. After this main phase, there is an additional "
                        + "combat phase followed by an additional main phase.");
            }
            case GREEN -> {
                card.setName("Wyll of the Fey Pact");
                card.setManaCost("{3}{R}{R}{G}");
                card.setColors(List.of(CardColor.RED, CardColor.GREEN));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.GREEN));
                card.setColor(CardColor.GREEN);
                card.setCardText("When this creature specializes, you may sacrifice another creature or an artifact. "
                        + "If you do, Wyll of the Fey Pact perpetually gets +3/+3 and gains trample.");
            }
        }
    }
}
