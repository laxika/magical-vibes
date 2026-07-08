package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnightOfMeadowgrainTest extends BaseCardTest {

    // ===== Lifelink =====

    @Test
    @DisplayName("Attacking a player gains controller life equal to combat damage dealt")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent knight = new Permanent(new KnightOfMeadowgrain());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Knight deals 2 combat damage: player2 loses 2, player1 gains 2 from lifelink
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== First strike =====

    @Test
    @DisplayName("First strike kills an equal-toughness blocker before it deals damage; Knight survives and gains life")
    void firstStrikeKillsBlockerAndKnightSurvives() {
        harness.setLife(player1, 20);

        Permanent knight = new Permanent(new KnightOfMeadowgrain());
        knight.setSummoningSick(false);
        knight.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Knight's 2 first strike damage kills the 2/2 Grizzly Bears before it can deal damage.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Knight survives unharmed.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Knight of Meadowgrain"));
        // Lifelink gains 2 life from the combat damage dealt to the blocker.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
