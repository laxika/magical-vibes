package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiTemporalArchmage.class, TeferiTemporalPilgrim.class, GrizzlyBears.class, Forest.class})
class TeferiTemporalArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts one of the top two cards into hand and the other on the bottom")
    void plusOneTakesOneOfTopTwo() {
        Permanent teferi = addReadyArchmage(player1, 3);
        Card kept = new GrizzlyBears();
        Card bottomed = new Forest();
        harness.setLibrary(player1, List.of(kept, bottomed));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).contains(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed);
    }

    @Test
    @DisplayName("-1 untaps up to four target permanents")
    void minusOneUntapsTargetPermanents() {
        Permanent teferi = addReadyArchmage(player1, 3);
        Permanent ownCreature = addTapped(player1, new GrizzlyBears());
        Permanent ownLand = addTapped(player1, new Forest());
        Permanent opposingCreature = addTapped(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(ownCreature.getId(), ownLand.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The emblem allows loyalty abilities of other planeswalkers on an opponent's turn")
    void emblemAllowsOtherPlaneswalkersAtInstantSpeed() {
        Permanent archmage = addReadyArchmage(player1, 10);
        Permanent pilgrim = addReadyPilgrim(player1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(archmage.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(pilgrim.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void plusOneWithOneCardPutsItIntoHandWithoutDrawing() {
        addReadyArchmage(player1, 3);
        Permanent pilgrim = addReadyPilgrim(player1);
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(pilgrim.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneWithEmptyLibraryDoesNotLoseTheGame() {
        Permanent teferi = addReadyArchmage(player1, 3);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void minusOneMayChooseNoTargets() {
        Permanent teferi = addReadyArchmage(player1, 3);
        Permanent land = addTapped(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusOneUntapsFourTargetsIncludingItself() {
        Permanent teferi = addReadyArchmage(player1, 3);
        teferi.tap();
        Permanent first = addTapped(player1, new Forest());
        Permanent second = addTapped(player2, new Forest());
        Permanent third = addTapped(player2, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(teferi.getId(), first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(List.of(teferi, first, second, third)).allMatch(p -> !p.isTapped());
    }

    @Test
    void emblemDoesNotAllowTwoLoyaltyActivationsInOneTurn() {
        addReadyArchmage(player1, 11);
        Permanent pilgrim = addReadyPilgrim(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pilgrim.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void emblemAllowsActivationInResponseToAnotherLoyaltyAbility() {
        addReadyArchmage(player1, 10);
        addReadyPilgrim(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent replacement = addReadyArchmage(player1, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(replacement.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void emblemAllowsTheSamePlaneswalkerOnConsecutivePlayersTurns() {
        addReadyArchmage(player1, 10);
        Permanent pilgrim = addReadyPilgrim(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(pilgrim.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        harness.performUntapStep(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pilgrim.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyArchmage(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TeferiTemporalArchmage());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addReadyPilgrim(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TeferiTemporalPilgrim());
        permanent.setCounterCount(CounterType.LOYALTY, 1);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.tap();
        return permanent;
    }

}
