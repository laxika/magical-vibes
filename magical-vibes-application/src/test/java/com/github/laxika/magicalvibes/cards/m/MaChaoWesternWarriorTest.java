package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
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

@CardUsed({MaChaoWesternWarrior.class, ShuCavalry.class, ForestBear.class})
class MaChaoWesternWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone makes Ma Chao unblockable this combat")
    void attacksAloneBecomesUnblockable() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Attacking alongside another creature does not make Ma Chao unblockable")
    void attacksWithOthersStaysBlockable() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());
        addCreatureReady(player1, new ShuCavalry());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Horsemanship prevents a creature without horsemanship from blocking Ma Chao")
    void horsemanshipPreventsNonHorsemanshipBlock() {
        Permanent blocker = addCreatureReady(player2, new ForestBear());
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());
        maChao.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(maChao)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Horsemanship allows a creature with horsemanship to block Ma Chao")
    void horsemanshipAllowsHorsemanshipBlock() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());
        maChao.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(maChao))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of combat")
    void unblockableResetsAtEndOfCombat() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maChao.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent maChao = addCreatureReady(player1, new MaChaoWesternWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(maChao.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maChao.isCantBeBlocked()).isFalse();
    }
}
