package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NecrodualityTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken Zombie entering creates a token copy")
    void nontokenZombieEnteringCreatesTokenCopy() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell; Necroduality triggers
        harness.passBothPriorities(); // resolve copy trigger

        long zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies"))
                .count();
        assertThat(zombies).isEqualTo(2);

        long tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies") && p.getCard().isToken())
                .count();
        assertThat(tokens).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy does not retrigger Necroduality")
    void tokenCopyDoesNotRetrigger() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Only one token — the copy's ETB must not fire Necroduality again.
        long tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies") && p.getCard().isToken())
                .count();
        assertThat(tokens).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Non-Zombie creature entering does not trigger")
    void nonZombieDoesNotTrigger() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        long bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(bears).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's nontoken Zombie entering does not trigger")
    void opponentZombieDoesNotTrigger() {
        harness.addToBattlefield(player1, new Necroduality());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new ScatheZombies()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        long zombies = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies"))
                .count();
        assertThat(zombies).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
