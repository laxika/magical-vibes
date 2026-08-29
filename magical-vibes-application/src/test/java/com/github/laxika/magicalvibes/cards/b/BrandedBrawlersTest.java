package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandedBrawlersTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while defending player controls an untapped land")
    void cannotAttackWhileDefendingPlayerControlsUntappedLand() {
        addCreatureReady(player1, new BrandedBrawlers());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack while defending player's lands are tapped")
    void canAttackWhileDefendingPlayersLandsAreTapped() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BrandedBrawlers());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot block while controller controls an untapped land")
    void cannotBlockWhileControllerControlsUntappedLand() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BrandedBrawlers());
        harness.addToBattlefield(player2, new Forest());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block while controller's lands are tapped")
    void canBlockWhileControllersLandsAreTapped() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BrandedBrawlers());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }
}
