package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TenthDistrictHero.class, GrizzlyBears.class, TurnToFrog.class})
class TenthDistrictHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Collect evidence 2 makes it a vigilant 4/4 Detective")
    void firstAbilityCollectsEvidenceAndTransformsHero() {
        Card evidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        addMana(ManaColor.WHITE, 1);
        addMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        assertThat(choice.validCardIds()).containsExactly(evidence.getId());
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HUMAN, CardSubtype.DETECTIVE);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(evidence);
    }

    @Test
    @DisplayName("Collect evidence 4 turns a Detective into Mileva and grants indestructible to other creatures")
    void secondAbilityCreatesMileva() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        Card thirdEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addMana(ManaColor.WHITE, 2);
        addMana(ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId()));
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of(secondEvidence.getId(), thirdEvidence.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSupertype(gd, hero, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Mileva, the Stalwart");
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The legendary transformation does nothing before it is a Detective")
    void secondAbilityRequiresDetective() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        addMana(ManaColor.WHITE, 1);
        addMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId(), secondEvidence.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSupertype(gd, hero, CardSupertype.LEGENDARY)).isFalse();
        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Tenth District Hero");
    }

    @Test
    @DisplayName("An evidence cost cannot be activated without enough graveyard mana value")
    void evidenceCostMustBePayable() {
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        addMana(ManaColor.WHITE, 1);
        addMana(ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("evidence cost");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hero);
    }

    @Test
    void firstAbilityReplacesCreatureTypesWhenResolvingAfterTurnToFrog() {
        Card evidence = new TenthDistrictHero();
        harness.setGraveyard(player1, List.of(evidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.DETECTIVE);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void losingAbilitiesDoesNotUndoMilevasNameChange() {
        Card firstEvidence = new TenthDistrictHero();
        Card secondEvidence = new TenthDistrictHero();
        Card thirdEvidence = new TenthDistrictHero();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        Permanent other = addCreatureReady(player1, new TenthDistrictHero());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId()));
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(secondEvidence.getId(), thirdEvidence.getId()));
        resolveAllTriggers();
        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Mileva, the Stalwart");
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, hero.getId());

        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Mileva, the Stalwart");
        assertThat(gqs.hasEffectiveSupertype(gd, hero, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void firstAbilityAfterMilevaSetsFourFourAndRetainsOtherChanges() {
        Card firstEvidence = new TenthDistrictHero();
        Card secondEvidence = new TenthDistrictHero();
        Card thirdEvidence = new TenthDistrictHero();
        Card fourthEvidence = new TenthDistrictHero();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence, fourthEvidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        Permanent other = addCreatureReady(player1, new TenthDistrictHero());
        Permanent opponent = addCreatureReady(player2, new TenthDistrictHero());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId()));
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(secondEvidence.getId(), thirdEvidence.getId()));
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(fourthEvidence.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Mileva, the Stalwart");
        assertThat(gqs.hasEffectiveSupertype(gd, hero, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void secondAbilityChecksDetectiveAtResolutionRatherThanActivation() {
        Card firstEvidence = new TenthDistrictHero();
        Card secondEvidence = new TenthDistrictHero();
        Card thirdEvidence = new TenthDistrictHero();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId(), secondEvidence.getId()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(thirdEvidence.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Mileva, the Stalwart");
        assertThat(gqs.hasEffectiveSupertype(gd, hero, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    void secondAbilityDoesNothingIfDetectiveTypeIsLostBeforeResolution() {
        Card firstEvidence = new TenthDistrictHero();
        Card secondEvidence = new TenthDistrictHero();
        Card thirdEvidence = new TenthDistrictHero();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence));
        Permanent hero = addCreatureReady(player1, new TenthDistrictHero());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId()));
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(secondEvidence.getId(), thirdEvidence.getId()));

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveName(gd, hero)).isEqualTo("Tenth District Hero");
        assertThat(gqs.hasEffectiveSupertype(gd, hero, CardSupertype.LEGENDARY)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).containsExactly(CardSubtype.FROG);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
