package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazyHomunculus.class, PygmyRazorback.class, RhysticCave.class})
class HazyHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Hazy Homunculus can't be blocked when defending player controls an untapped land")
    void cannotBeBlockedWhenDefenderControlsUntappedLand() {
        harness.addToBattlefield(player2, new RhysticCave());
        Permanent blocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent homunculus = addAttackingHomunculus();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(homunculus)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Hazy Homunculus can be blocked when defending player controls only tapped lands")
    void canBeBlockedWhenDefenderControlsTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        land.tap();
        Permanent blocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent homunculus = addAttackingHomunculus();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(homunculus))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hazy Homunculus can be blocked when defending player controls no lands")
    void canBeBlockedWhenDefenderControlsNoLand() {
        Permanent blocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent homunculus = addAttackingHomunculus();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(homunculus))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hazy Homunculus can be blocked when only the attacking player controls a land")
    void canBeBlockedWhenOnlyAttackingPlayerControlsLand() {
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent blocker = addCreatureReady(player2, new PygmyRazorback());
        Permanent homunculus = addAttackingHomunculus();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(homunculus))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingHomunculus() {
        Permanent homunculus = addCreatureReady(player1, new HazyHomunculus());
        homunculus.setAttacking(true);
        return homunculus;
    }
}
