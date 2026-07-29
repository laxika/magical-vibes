package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GrimFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller at the beginning of their upkeep")
    void dealsOneDamageAtControllerUpkeep() {
        harness.addToBattlefield(player1, new GrimFeast());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Does not trigger at the opponent's upkeep")
    void doesNotTriggerAtOpponentUpkeep() {
        harness.addToBattlefield(player1, new GrimFeast());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Controller gains life equal to the toughness of a dying opponent creature")
    void gainsLifeEqualToDyingOpponentCreatureToughness() {
        harness.addToBattlefield(player1, new GrimFeast());
        harness.addToBattlefield(player2, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID thopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castInstant(player1, 0, thopterId);
        harness.passBothPriorities(); // Shock resolves, Ornithopter dies, trigger goes on the stack
        harness.passBothPriorities(); // Life-gain trigger resolves

        // Ornithopter is 0/2 — the gain follows toughness, not power.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature dies")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new GrimFeast());
        harness.addToBattlefield(player1, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID thopterId = harness.getPermanentId(player1, "Ornithopter");
        harness.castInstant(player2, 0, thopterId);
        harness.passBothPriorities(); // Shock resolves, Ornithopter dies

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }
}
