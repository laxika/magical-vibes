package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SeekInstantOrSorceryAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeKarlachEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "14")
public class KarlachRagingTiefling extends Card {

    public KarlachRagingTiefling() {
        addSpecializeAbility(CardColor.WHITE);
        addSpecializeAbility(CardColor.BLUE);
        addSpecializeAbility(CardColor.BLACK);
        addSpecializeAbility(CardColor.RED);
        addSpecializeAbility(CardColor.GREEN);
    }

    private void addSpecializeAbility(CardColor color) {
        addActivatedAbility(createSpecializeAbility(color));
        addGraveyardActivatedAbility(createSpecializeAbility(color));
    }

    private static ActivatedAbility createSpecializeAbility(CardColor color) {
        return new ActivatedAbility(
                false,
                "{6}",
                List.of(new SpecializeKarlachEffect(color)),
                "Rage Beyond Death — Specialize {6}",
                ActivationTimingRestriction.SORCERY_SPEED);
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return switch (color) {
            case BLACK -> new MayEffect(
                    new SacrificePermanentThenEffect(
                            new PermanentIsCreaturePredicate(),
                            SequenceEffect.of(new DrawCardEffect(2),
                                    new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)),
                            "a creature", false, false),
                    "Sacrifice a creature?");
            case BLUE -> new SeekInstantOrSorceryAndMayCastFreeEffect();
            case RED -> new CantBlockThisTurnEffect(TapUntapScope.TARGET);
            case GREEN -> new BoostTargetCreatureEffect(4, 4, anotherCreatureYouControl());
            case WHITE -> SequenceEffect.of(
                    new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                            List.of(CardSubtype.KNIGHT), Set.of(), Set.of()),
                    new BoostAllOwnCreaturesEffect(1, 1),
                    new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));
        };
    }

    public static TargetFilter specializedTargetFilter(CardColor color) {
        return switch (color) {
            case RED -> TargetFilters.creatureAnOpponentControls();
            case GREEN -> new ControlledPermanentPredicateTargetFilter(
                    anotherCreatureYouControl(), "Target must be another creature you control");
            default -> null;
        };
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("14");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.TIEFLING, CardSubtype.BARBARIAN));
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setColorIdentity(List.of(CardColor.RED));
        card.setKeywords(Set.of(Keyword.FIRST_STRIKE, Keyword.HASTE));
    }

    private static PermanentPredicate anotherCreatureYouControl() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }
}
