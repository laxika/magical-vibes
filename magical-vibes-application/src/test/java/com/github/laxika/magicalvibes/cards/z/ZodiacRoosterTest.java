package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZodiacRooster.class, ShuFootSoldiers.class, Plains.class, Island.class})
class ZodiacRoosterTest extends BaseCardTest {

    // ===== Plainswalk =====

    @Test
    @DisplayName("Zodiac Rooster cannot be blocked when defending player controls a Plains")
    void cannotBeBlockedWhenDefenderControlsPlains() {
        harness.addToBattlefield(player2, new Plains());

        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacRooster());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Zodiac Rooster can be blocked when defending player does not control a Plains")
    void canBeBlockedWhenDefenderDoesNotControlPlains() {
        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacRooster());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Zodiac Rooster can be blocked when only the attacking player controls a Plains")
    void canBeBlockedWhenOnlyAttackerControlsPlains() {
        harness.addToBattlefield(player1, new Plains());

        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacRooster());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Zodiac Rooster can be blocked when defending player controls an Island")
    void canBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new ShuFootSoldiers());

        Permanent atkPerm = addCreatureReady(player1, new ZodiacRooster());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
