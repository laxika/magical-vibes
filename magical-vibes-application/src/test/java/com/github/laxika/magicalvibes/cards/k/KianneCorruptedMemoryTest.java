package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KianneCorruptedMemory.class, GrizzlyBears.class, Shock.class})
class KianneCorruptedMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("At even power, Kianne grants flash to noncreature spells")
    void evenPowerGrantsNoncreatureFlash() {
        harness.addToBattlefield(player1, new KianneCorruptedMemory());
        prepareOpponentTurn();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("At even power, Kianne does not grant flash to creature spells")
    void evenPowerDoesNotGrantCreatureFlash() {
        harness.addToBattlefield(player1, new KianneCorruptedMemory());
        prepareOpponentTurn();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Kianne")
    void drawingPutsCounterOnKianne() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At odd power, Kianne grants flash to creature spells")
    void oddPowerGrantsCreatureFlash() {
        harness.addToBattlefield(player1, new KianneCorruptedMemory());
        advanceToDraw(player1);
        harness.passBothPriorities();
        prepareOpponentTurn();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
