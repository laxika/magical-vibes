package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.w.WellOfLife;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeteranBrawlers.class, PygmyRazorback.class, RhysticCave.class, WellOfLife.class})
class VeteranBrawlersTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while defending player controls an untapped land")
    void cannotAttackWhileDefendingPlayerControlsUntappedLand() {
        addCreatureReady(player1, new VeteranBrawlers());
        harness.addToBattlefield(player2, new RhysticCave());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack while defending player's lands are tapped")
    void canAttackWhileDefendingPlayersLandsAreTapped() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VeteranBrawlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        land.tap();

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Can attack while only attacking player controls an untapped land")
    void canAttackWhileOnlyAttackingPlayerControlsUntappedLand() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VeteranBrawlers());
        harness.addToBattlefield(player1, new RhysticCave());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Can attack while defending player controls only an untapped nonland")
    void canAttackWhileDefendingPlayerControlsOnlyUntappedNonland() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VeteranBrawlers());
        harness.addToBattlefield(player2, new WellOfLife());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot block while controller controls an untapped land")
    void cannotBlockWhileControllerControlsUntappedLand() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        addCreatureReady(player2, new VeteranBrawlers());
        harness.addToBattlefield(player2, new RhysticCave());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block while controller's lands are tapped")
    void canBlockWhileControllersLandsAreTapped() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VeteranBrawlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        land.tap();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block while only attacking player controls an untapped land")
    void canBlockWhileOnlyAttackingPlayerControlsUntappedLand() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent blocker = addCreatureReady(player2, new VeteranBrawlers());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block while attacking player controls only an untapped nonland")
    void canBlockWhileAttackingPlayerControlsOnlyUntappedNonland() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new PygmyRazorback());
        Permanent blocker = addCreatureReady(player2, new VeteranBrawlers());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
