package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KinsbaileAspirant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravelgillScoundrel.class, KinsbaileAspirant.class})
class GravelgillScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Merfolk makes Gravelgill Scoundrel unblockable")
    void tappingAnotherMerfolkMakesItUnblockable() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());
        Permanent merfolk = addCreatureReady(player1, new GravelgillScoundrel());
        addCreatureReady(player1, new GravelgillScoundrel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, merfolk.getId());

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(scoundrel.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Declining the tap leaves Gravelgill Scoundrel blockable")
    void decliningTapLeavesItBlockable() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());
        Permanent merfolk = addCreatureReady(player1, new GravelgillScoundrel());
        addCreatureReady(player1, new GravelgillScoundrel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(merfolk.isTapped()).isFalse();
        assertThat(scoundrel.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attacking Scoundrel cannot be tapped for its ability")
    void requiresAnotherMerfolk() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scoundrel.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());
        Permanent merfolk = addCreatureReady(player1, new GravelgillScoundrel());
        addCreatureReady(player1, new GravelgillScoundrel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, merfolk.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(scoundrel.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An untapped non-Merfolk creature can pay even while summoning sick")
    void canTapSummoningSickNonMerfolk() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new KinsbaileAspirant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kithkin.isTapped()).isTrue();
        assertThat(scoundrel.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A single eligible creature is tapped automatically when payment is accepted")
    void tapsOnlyEligibleCreature() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new GravelgillScoundrel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(scoundrel.isTapped()).isFalse();
        assertThat(scoundrel.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay for the ability")
    void cannotTapTappedOrOpposingCreature() {
        Permanent scoundrel = addCreatureReady(player1, new GravelgillScoundrel());
        Permanent tapped = addCreatureReady(player1, new GravelgillScoundrel());
        tapped.tap();
        Permanent opposing = addCreatureReady(player2, new GravelgillScoundrel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scoundrel.isCantBeBlocked()).isFalse();
        assertThat(scoundrel.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
    }
}
