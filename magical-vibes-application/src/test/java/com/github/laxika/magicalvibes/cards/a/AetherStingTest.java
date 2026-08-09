package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherStingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage when an opponent casts a creature spell")
    void opponentCreatureSpellDealsDamage() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's noncreature spell")
    void opponentNoncreatureSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for the controller's creature spell")
    void controllerCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AetherSting());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new AetherSting());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
