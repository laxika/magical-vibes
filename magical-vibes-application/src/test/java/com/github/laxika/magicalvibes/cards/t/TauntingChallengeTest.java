package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TauntingChallenge.class, ForestBear.class, ShuCavalry.class})
class TauntingChallengeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving sets the must-be-blocked-by-all flag on the target")
    void resolvingSetsFlag() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForestBear());

        harness.setHand(player1, List.of(new TauntingChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Flag wears off at end of turn")
    void flagWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForestBear());

        harness.setHand(player1, List.of(new TauntingChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Resolving requires every able blocker to block the targeted attacker")
    void resolvingRequiresEveryAbleBlockerToBlock() {
        Permanent attacker = addCreatureReady(player1, new ForestBear());
        Permanent firstBlocker = addCreatureReady(player2, new ForestBear());
        Permanent secondBlocker = addCreatureReady(player2, new ForestBear());

        harness.setHand(player1, List.of(new TauntingChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An able blocker cannot decline to block the targeted attacker")
    void ableBlockerCannotDeclineToBlock() {
        Permanent attacker = addCreatureReady(player1, new ForestBear());
        addCreatureReady(player2, new ForestBear());

        harness.setHand(player1, List.of(new TauntingChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("All able creatures must block the affected attacker")
    void allAbleCreaturesMustBlock() {
        Permanent attacker = addCreatureReady(player1, new ForestBear());
        attacker.setMustBeBlockedByAllThisTurn(true);
        attacker.setAttacking(true);

        addCreatureReady(player2, new ForestBear());
        addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();

        // Only one blocker assigned — illegal, both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not required to block")
    void tappedCreaturesAreNotRequiredToBlock() {
        Permanent attacker = addCreatureReady(player1, new ForestBear());
        Permanent untapped = addCreatureReady(player2, new ForestBear());
        Permanent tapped = addCreatureReady(player2, new ForestBear());
        tapped.tap();

        harness.setHand(player1, List.of(new TauntingChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures unable to block the attacker are not required to block")
    void unableCreaturesAreNotRequiredToBlock() {
        Permanent attacker = addCreatureReady(player1, new ShuCavalry());
        Permanent unableBlocker = addCreatureReady(player2, new ForestBear());
        Permanent ableBlocker = addCreatureReady(player2, new ShuCavalry());

        harness.setHand(player1, List.of(new TauntingChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(unableBlocker.isBlocking()).isFalse();
        assertThat(ableBlocker.isBlocking()).isTrue();
    }
}
