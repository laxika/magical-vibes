package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfMeadowgrain.class, GoldmeadowStalwart.class})
class KnightOfMeadowgrainTest extends BaseCardTest {

    // ===== Lifelink =====

    @Test
    @DisplayName("Attacking a player gains controller life equal to combat damage dealt")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new KnightOfMeadowgrain());
        declareAttackers(List.of(0));

        // Knight deals 2 combat damage: player2 loses 2, player1 gains 2 from lifelink
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== First strike =====

    @Test
    @DisplayName("First strike kills an equal-toughness blocker before it deals damage; Knight survives and gains life")
    void firstStrikeKillsBlockerAndKnightSurvives() {
        harness.setLife(player1, 20);

        Permanent knight = addCreatureReady(player1, new KnightOfMeadowgrain());
        knight.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GoldmeadowStalwart());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Knight's 2 first strike damage kills the 2/2 Goldmeadow Stalwart before it can deal damage.
        harness.assertNotOnBattlefield(player2, "Goldmeadow Stalwart");
        // Knight survives unharmed.
        harness.assertOnBattlefield(player1, "Knight of Meadowgrain");
        // Lifelink gains 2 life from the combat damage dealt to the blocker.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
