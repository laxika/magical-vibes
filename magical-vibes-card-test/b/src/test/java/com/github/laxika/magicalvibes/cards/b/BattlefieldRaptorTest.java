package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElderfangDisciple;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldRaptor.class, ElderfangDisciple.class})
class BattlefieldRaptorTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new BattlefieldRaptor());
        addCreatureReady(player2, new ElderfangDisciple());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Battlefield Raptor (flying)");
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new BattlefieldRaptor());
        Permanent blocker = addCreatureReady(player2, new BattlefieldRaptor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Battlefield Raptor");
        harness.assertOnBattlefield(player2, "Battlefield Raptor");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeKillsAttackerBeforeItDealsDamage() {
        addCreatureReady(player1, new ElderfangDisciple());
        Permanent raptor = addCreatureReady(player2, new BattlefieldRaptor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Elderfang Disciple");
        harness.assertNotOnBattlefield(player1, "Elderfang Disciple");
        harness.assertOnBattlefield(player2, "Battlefield Raptor");
        assertThat(raptor.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new BattlefieldRaptor());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
    }
}
