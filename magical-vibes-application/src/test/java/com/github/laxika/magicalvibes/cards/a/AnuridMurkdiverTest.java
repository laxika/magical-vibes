package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnuridMurkdiver.class, ElvishWarrior.class, Swamp.class})
class AnuridMurkdiverTest extends BaseCardTest {

    @Test
    @DisplayName("Anurid Murkdiver can't be blocked when defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        Permanent attacker = addCreatureReady(player1, new AnuridMurkdiver());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Anurid Murkdiver can be blocked when defending player controls no Swamp")
    void canBeBlockedWhenDefenderControlsNoSwamp() {
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        Permanent attacker = addCreatureReady(player1, new AnuridMurkdiver());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Anurid Murkdiver can be blocked when only the attacking player controls a Swamp")
    void canBeBlockedWhenOnlyAttackerControlsSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        Permanent attacker = addCreatureReady(player1, new AnuridMurkdiver());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
