package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.j.JackalFamiliar;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrizedUnicorn.class, RuneclawBear.class, JackalFamiliar.class})
class PrizedUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Prized Unicorn")
    void allAbleCreaturesMustBlock() {
        Permanent unicorn = addCreatureReady(player1, new PrizedUnicorn());
        unicorn.setAttacking(true);

        addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        // Only one blocker assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both blockers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Prized Unicorn")
    void tappedCreaturesNotForcedToBlock() {
        Permanent unicorn = addCreatureReady(player1, new PrizedUnicorn());
        unicorn.setAttacking(true);

        Permanent untapped = addCreatureReady(player2, new RuneclawBear());
        Permanent tapped = addCreatureReady(player2, new RuneclawBear());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("No blockers required when defender has no creatures")
    void noBlockersWhenDefenderHasNoCreatures() {
        Permanent unicorn = addCreatureReady(player1, new PrizedUnicorn());
        unicorn.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.creaturesBlockedThisTurn).doesNotContain(unicorn.getId());
    }

    @Test
    @DisplayName("Blockers cannot choose another attacker instead of Prized Unicorn")
    void cannotDivertBlockerToAnotherAttacker() {
        addCreatureReady(player1, new PrizedUnicorn()).setAttacking(true);
        addCreatureReady(player1, new RuneclawBear()).setAttacking(true);
        addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.creaturesBlockedThisTurn).contains(gd.playerBattlefields.get(player1.getId()).get(0).getId());
        assertThat(gd.creaturesBlockedThisTurn).doesNotContain(gd.playerBattlefields.get(player1.getId()).get(1).getId());
    }

    @Test
    @DisplayName("Two attacking Unicorns allow blockers to divide between them")
    void competingRequirementsAllowDividingBlockers() {
        addCreatureReady(player1, new PrizedUnicorn()).setAttacking(true);
        addCreatureReady(player1, new PrizedUnicorn()).setAttacking(true);
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature that cannot block alone is not forced to make an illegal block")
    void loneJackalIsNotForcedToBlock() {
        Permanent unicorn = addCreatureReady(player1, new PrizedUnicorn());
        unicorn.setAttacking(true);
        Permanent jackal = addCreatureReady(player2, new JackalFamiliar());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(jackal.isBlocking()).isFalse();
        assertThat(gd.creaturesBlockedThisTurn).doesNotContain(unicorn.getId());
    }

    @Test
    @DisplayName("A creature that cannot block alone must block when another blocker is available")
    void jackalMustBlockWithAvailablePartner() {
        addCreatureReady(player1, new PrizedUnicorn()).setAttacking(true);
        Permanent jackal = addCreatureReady(player2, new JackalFamiliar());
        Permanent bear = addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(jackal.isBlocking()).isTrue();
        assertThat(bear.isBlocking()).isTrue();
    }
}
