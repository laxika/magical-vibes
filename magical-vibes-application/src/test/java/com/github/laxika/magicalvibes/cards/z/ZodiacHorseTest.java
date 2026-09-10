package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZodiacHorse.class, ShuFootSoldiers.class, Island.class})
class ZodiacHorseTest extends BaseCardTest {

    @Test
    @DisplayName("Zodiac Horse cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacHorse());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Zodiac Horse can be blocked when defending player does not control an Island")
    void canBeBlockedWhenDefenderDoesNotControlIsland() {
        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacHorse());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Zodiac Horse can be blocked when only the attacking player controls an Island")
    void canBeBlockedWhenOnlyAttackerControlsIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacHorse());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
