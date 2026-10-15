package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.z.ZooEscapees;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AprilONeilKunoichiTrainee.class, ZooEscapees.class})
class AprilONeilKunoichiTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prompts to scry 2")
    void scriesTwoOnEnter() {
        harness.setLibrary(player1, List.of(new ZooEscapees(), new ZooEscapees()));

        harness.setHand(player1, List.of(new AprilONeilKunoichiTrainee()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Cannot be blocked by a creature with power 3")
    void cannotBeBlockedByPowerThreeCreature() {
        Permanent blocker = addCreatureReady(player2, new ZooEscapees());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent april = addCreatureReady(player1, new AprilONeilKunoichiTrainee());
        april.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(april);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power less than 3")
    void canBeBlockedByPowerLessThanThreeCreature() {
        Permanent blocker = addCreatureReady(player2, new ZooEscapees());
        Permanent april = addCreatureReady(player1, new AprilONeilKunoichiTrainee());
        april.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(april);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Scry can reverse both cards on top")
    void canReorderTopCards() {
        Card first = new ZooEscapees();
        Card second = new ZooEscapees();
        Card third = new ZooEscapees();
        harness.setLibrary(player1, List.of(first, second, third));
        castAprilAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
    }

    @Test
    @DisplayName("Scry can put both cards on the bottom in either order")
    void canBottomBothCardsInChosenOrder() {
        Card first = new ZooEscapees();
        Card second = new ZooEscapees();
        Card third = new ZooEscapees();
        harness.setLibrary(player1, List.of(first, second, third));
        castAprilAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    @DisplayName("Scry can keep one card and bottom the other")
    void canSplitScryCards() {
        Card first = new ZooEscapees();
        Card second = new ZooEscapees();
        Card third = new ZooEscapees();
        harness.setLibrary(player1, List.of(first, second, third));
        castAprilAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Scry 2 with one card looks at only that card")
    void scriesAvailableCardInShortLibrary() {
        Card onlyCard = new ZooEscapees();
        harness.setLibrary(player1, List.of(onlyCard));
        castAprilAndResolve();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Power greater than 3 also prevents blocking")
    void cannotBeBlockedByPowerFiveCreature() {
        Permanent blocker = addCreatureReady(player2, new ZooEscapees());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent april = addCreatureReady(player1, new AprilONeilKunoichiTrainee());
        april.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature whose power falls below 3 can block")
    void blockerPowerIsCheckedAtDeclaration() {
        Permanent blocker = addCreatureReady(player2, new ZooEscapees());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent april = addCreatureReady(player1, new AprilONeilKunoichiTrainee());
        april.setAttacking(true);
        prepareDeclareBlockers();
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castAprilAndResolve() {
        harness.setHand(player1, List.of(new AprilONeilKunoichiTrainee()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
