package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BrandedBrawlers.class, PygmyRazorback.class, RhysticCave.class})
class BrandedBrawlersTest extends BaseCardTest {

    @Test
    @DisplayName("One untapped land prevents attacking even among tapped lands")
    void cannotAttackWithMixedDefendingLandStates() {
        addCreatureReady(player1, new BrandedBrawlers());
        harness.addToBattlefieldAndReturn(player2, new RhysticCave()).tap();
        harness.addToBattlefield(player2, new RhysticCave());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapped nonland permanents do not prevent attacking")
    void canAttackWithUntappedDefendingNonland() {
        addCreatureReady(player1, new BrandedBrawlers());
        harness.addToBattlefield(player2, new PygmyRazorback());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isAttacking()).isTrue();
    }

    @Test
    @DisplayName("One untapped land prevents blocking even among tapped lands")
    void cannotBlockWithMixedControllerLandStates() {
        addCreatureReady(player1, new PygmyRazorback()).setAttacking(true);
        addCreatureReady(player2, new BrandedBrawlers());
        harness.addToBattlefieldAndReturn(player2, new RhysticCave()).tap();
        harness.addToBattlefield(player2, new RhysticCave());
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapped nonland permanents do not prevent blocking")
    void canBlockWithUntappedControllerNonland() {
        addCreatureReady(player1, new PygmyRazorback()).setAttacking(true);
        addCreatureReady(player2, new BrandedBrawlers());
        harness.addToBattlefield(player2, new PygmyRazorback());
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack while defending player controls an untapped land")
    void cannotAttackWhileDefendingPlayerControlsUntappedLand() {
        addCreatureReady(player1, new BrandedBrawlers());
        harness.addToBattlefield(player2, new RhysticCave());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack while defending player's lands are tapped")
    void canAttackWhileDefendingPlayersLandsAreTapped() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BrandedBrawlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        land.tap();

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can attack when only the attacking player controls an untapped land")
    void canAttackWhenOnlyAttackingPlayerControlsUntappedLand() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BrandedBrawlers());
        harness.addToBattlefield(player1, new RhysticCave());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot block while controller controls an untapped land")
    void cannotBlockWhileControllerControlsUntappedLand() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BrandedBrawlers());
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
        addCreatureReady(player2, new BrandedBrawlers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        land.tap();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block when only the attacking player controls an untapped land")
    void canBlockWhenOnlyAttackingPlayerControlsUntappedLand() {
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BrandedBrawlers());
        harness.addToBattlefield(player1, new RhysticCave());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }
}
