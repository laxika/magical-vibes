package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardsUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeWyllEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
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
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GainControlOfTargetEffect(ControlDuration.UNTIL_END_OF_YOUR_NEXT_TURN));

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
                        new SpecializeWyllEffect(color)),
                "Specialize {2} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return new MayEffect(
                new SacrificePermanentThenEffect(
                        anotherCreatureOrArtifact(), specializedThenEffect(color),
                        "another creature or an artifact"),
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
            case BLACK -> new DrawCardsUnlessTargetPaysLifeEffect(3, 5);
            case RED -> SequenceEffect.of(
                    new UntapPermanentsEffect(TapUntapScope.SELF),
                    new AdditionalCombatMainPhaseEffect(1));
            case GREEN -> SequenceEffect.of(
                    new PerpetuallyBoostSourceEffect(3, 3),
                    new PerpetuallyGrantKeywordToSourceEffect(Keyword.TRAMPLE));
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
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsArtifactPredicate())),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }
}
