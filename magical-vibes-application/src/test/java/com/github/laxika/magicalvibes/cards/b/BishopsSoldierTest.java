package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BishopsSoldier.class, RaptorCompanion.class})
class BishopsSoldierTest extends BaseCardTest {

    @Test
    void unblockedCombatDamageGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BishopsSoldier());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void blockedSoldierGainsFullDamageAmountEvenWhenBothCreaturesDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BishopsSoldier());
        harness.addToBattlefield(player2, new RaptorCompanion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Bishop's Soldier");
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    void blockingSoldierGainsLifeForDefendingControllerBeforeDying() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RaptorCompanion());
        harness.addToBattlefield(player2, new BishopsSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Raptor Companion");
        harness.assertInGraveyard(player2, "Bishop's Soldier");
    }
}
