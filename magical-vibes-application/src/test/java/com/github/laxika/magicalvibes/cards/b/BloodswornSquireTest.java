package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodswornSquire.class, BloodswornKnight.class, GrizzlyBears.class, Shock.class})
class BloodswornSquireTest extends BaseCardTest {

    @Test
    void abilityTapsAndGrantsIndestructibleWithoutTransformingBelowThreshold() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(squire.isTransformed()).isFalse();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void transformsWhenFourCreatureCardsAreInGraveyard() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isTrue();
        assertThat(squire.getCard().getName()).isEqualTo("Bloodsworn Knight");
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(4);
    }

    @Test
    void backFaceAbilityTapsAndGrantsIndestructible() {
        BloodswornSquire card = new BloodswornSquire();
        Permanent knight = harness.addToBattlefieldAndReturn(player1, card);
        knight.setCard(card.getBackFaceCard());
        knight.setTransformed(true);
        knight.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(knight.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(knight.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    void discardedCreatureCountsButProtectionAndTapWaitForResolution() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.setHand(player1, List.of(new BloodswornSquire()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(squire.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(squire.isTransformed()).isFalse();

        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isTrue();
        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(4);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent squire = harness.addToBattlefieldAndReturn(player1, new BloodswornSquire());
        squire.setSummoningSick(true);
        squire.setTapped(true);
        harness.setHand(player1, List.of(new BloodswornSquire()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(squire.isTransformed()).isFalse();
    }

    @Test
    void checksCreatureThresholdAtResolutionAndIgnoresOpponentsGraveyard() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.setGraveyard(player2, List.of(
                new BloodswornSquire(), new BloodswornSquire(),
                new BloodswornSquire(), new BloodswornSquire()));
        harness.setHand(player1, List.of(new BloodswornSquire()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isFalse();
        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void stackedFrontFaceActivationsDoNotTransformItBack() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(),
                new BloodswornSquire(), new BloodswornSquire()));
        harness.setHand(player1, List.of(new BloodswornSquire(), new BloodswornSquire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isTrue();
        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(6);
    }

    @Test
    void knightTracksOnlyControllersCreatureCardsAndDiesAtZeroDespiteIndestructible() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.setHand(player1, List.of(new BloodswornSquire()));
        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setGraveyard(player2, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.setGraveyard(player1, List.of(new BloodswornSquire(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Shock()));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Bloodsworn Knight");
        harness.assertInGraveyard(player1, "Bloodsworn Squire");
    }

    @Test
    void knightAbilityCanDiscardCreatureToIncreaseItsSizeWithoutTransformingBack() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.setHand(player1, List.of(new BloodswornSquire(), new BloodswornSquire()));
        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.isTransformed()).isTrue();
        assertThat(squire.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(5);
    }

    @Test
    void indestructiblePreventsDestructionFromLethalDamage() {
        Permanent squire = addSquireReady();
        harness.setHand(player1, List.of(new BloodswornSquire()));
        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, squire.getId());
        harness.castAndResolveInstant(player2, 0, squire.getId());

        harness.assertOnBattlefield(player1, "Bloodsworn Squire");
        assertThat(squire.isTransformed()).isFalse();
        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void indestructibleExpiresAtEndOfTurnWithoutRevertingTransformation() {
        Permanent squire = addSquireReady();
        harness.setGraveyard(player1, List.of(
                new BloodswornSquire(), new BloodswornSquire(), new BloodswornSquire()));
        harness.setHand(player1, List.of(new BloodswornSquire()));
        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, squire, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(squire.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(4);
    }

    private Permanent addSquireReady() {
        return addCreatureReady(player1, new BloodswornSquire());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
