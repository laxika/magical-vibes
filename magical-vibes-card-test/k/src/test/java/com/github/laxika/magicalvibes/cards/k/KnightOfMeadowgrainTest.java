package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfMeadowgrain.class, GoldmeadowStalwart.class})
class KnightOfMeadowgrainTest extends BaseCardTest {

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

    @Test
    @DisplayName("First strike kills an equal-toughness blocker before it deals damage; Knight survives and gains life")
    void firstStrikeKillsBlockerAndKnightSurvives() {
        harness.setLife(player1, 20);

        Permanent knight = addCreatureReady(player1, new KnightOfMeadowgrain());
        knight.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GoldmeadowStalwart());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Knight's 2 first strike damage kills the 2/2 Goldmeadow Stalwart before it can deal damage.
        harness.assertNotOnBattlefield(player2, "Goldmeadow Stalwart");
        // Knight survives unharmed.
        harness.assertOnBattlefield(player1, "Knight of Meadowgrain");
        // Lifelink gains 2 life from the combat damage dealt to the blocker.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Blocking Knight kills an attacker before regular damage and gains life for its controller")
    void firstStrikeAndLifelinkWorkWhenBlocking() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new GoldmeadowStalwart());
        attacker.setAttacking(true);
        Permanent knight = addCreatureReady(player2, new KnightOfMeadowgrain());
        knight.setBlocking(true);
        knight.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Goldmeadow Stalwart");
        harness.assertOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Opposing Knights deal simultaneous first strike damage and both gain life before dying")
    void bothKnightsGainLifeDespiteDyingInFirstStrikeCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new KnightOfMeadowgrain());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KnightOfMeadowgrain());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Knight of Meadowgrain");
        harness.assertNotOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertInGraveyard(player1, "Knight of Meadowgrain");
        harness.assertInGraveyard(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
    }
}
