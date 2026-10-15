package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChargingSlateback;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TauntingElf.class, ElvishWarrior.class, ChargingSlateback.class})
class TauntingElfTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Taunting Elf")
    void allAbleCreaturesMustBlock() {
        Permanent elf = addCreatureReady(player1, new TauntingElf());
        elf.setAttacking(true);

        addCreatureReady(player2, new ElvishWarrior());
        addCreatureReady(player2, new ElvishWarrior());

        prepareDeclareBlockers();

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
    @DisplayName("Tapped creatures are not forced to block Taunting Elf")
    void tappedCreaturesAreNotForcedToBlock() {
        Permanent elf = addCreatureReady(player1, new TauntingElf());
        elf.setAttacking(true);

        Permanent untapped = addCreatureReady(player2, new ElvishWarrior());
        Permanent tapped = addCreatureReady(player2, new ElvishWarrior());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures that cannot block are not forced to block Taunting Elf")
    void creaturesThatCannotBlockAreNotForcedToBlock() {
        Permanent elf = addCreatureReady(player1, new TauntingElf());
        elf.setAttacking(true);

        Permanent able = addCreatureReady(player2, new ElvishWarrior());
        Permanent unable = addCreatureReady(player2, new ChargingSlateback());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(able.isBlocking()).isTrue();
        assertThat(unable.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Able blockers cannot block another attacker instead of Taunting Elf")
    void cannotDivertBlockersToAnotherAttacker() {
        addCreatureReady(player1, new TauntingElf()).setAttacking(true);
        addCreatureReady(player1, new ElvishWarrior()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Blockers may split between two attacking Taunting Elves")
    void blockersMaySplitBetweenTwoElves() {
        addCreatureReady(player1, new TauntingElf()).setAttacking(true);
        addCreatureReady(player1, new TauntingElf()).setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new ElvishWarrior());
        Permanent secondBlocker = addCreatureReady(player2, new ElvishWarrior());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick creatures must still block Taunting Elf")
    void summoningSicknessDoesNotPreventRequiredBlock() {
        addCreatureReady(player1, new TauntingElf()).setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        blocker.setSummoningSick(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A nonattacking Taunting Elf does not constrain blockers")
    void nonattackingElfDoesNotConstrainBlockers() {
        addCreatureReady(player1, new TauntingElf());
        addCreatureReady(player1, new ElvishWarrior()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
