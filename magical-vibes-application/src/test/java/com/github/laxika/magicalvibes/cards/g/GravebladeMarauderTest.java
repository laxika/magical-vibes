package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravebladeMarauder.class, TimberpackWolf.class, FieryImpulse.class})
class GravebladeMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player loses life equal to creature cards in the controller's graveyard")
    void damagedPlayerLosesLifePerCreatureCardInGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new TimberpackWolf(), new TimberpackWolf(), new FieryImpulse()));

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // 1 combat damage: 20 -> 19. Two creature cards in the graveyard: 19 -> 17.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("No life loss beyond combat damage with an empty graveyard")
    void noExtraLifeLossWithEmptyGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of());

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Only the controller's graveyard is counted, not the opponent's")
    void opponentGraveyardIsNotCounted() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new TimberpackWolf(), new TimberpackWolf()));

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No trigger when the attacker is blocked and deals no damage to a player")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new TimberpackWolf(), new TimberpackWolf()));

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new TimberpackWolf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Timberpack Wolf");
        harness.assertOnBattlefield(player1, "Graveblade Marauder");
    }

    @Test
    @DisplayName("Creature cards are counted when the combat damage trigger resolves")
    void countsGraveyardAtResolution() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of());
        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new TimberpackWolf(), new TimberpackWolf()));
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The combat damage trigger still resolves after Marauder leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new TimberpackWolf()));
        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(marauder);
        harness.setGraveyard(player1, List.of(new TimberpackWolf(), marauder.getCard()));
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }
}
