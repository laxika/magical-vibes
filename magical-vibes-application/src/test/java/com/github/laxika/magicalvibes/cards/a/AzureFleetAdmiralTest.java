package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzureFleetAdmiral.class, GrizzlyBears.class})
class AzureFleetAdmiralTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new AzureFleetAdmiral());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("It can't be blocked by a creature controlled by the monarch")
    void cannotBeBlockedByCreatureControlledByMonarch() {
        gd.monarchPlayerId = player2.getId();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = attackingAdmiral();

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("It can be blocked by a creature not controlled by the monarch")
    void canBeBlockedByCreatureNotControlledByMonarch() {
        gd.monarchPlayerId = player1.getId();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = attackingAdmiral();

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingAdmiral() {
        Permanent attacker = addCreatureReady(player1, new AzureFleetAdmiral());
        attacker.setAttacking(true);
        return attacker;
    }
}
