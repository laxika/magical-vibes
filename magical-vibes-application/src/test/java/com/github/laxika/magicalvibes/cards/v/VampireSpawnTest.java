package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VampireSpawn.class)
class VampireSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent lose 2 life and its controller gain 2 life")
    void entersBattlefieldDrainsOpponentsAndGainsLife() {
        castVampireSpawn();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB trigger is put on the stack after the creature resolves")
    void entersBattlefieldPutsTriggerOnStack() {
        castVampireSpawn();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    private void castVampireSpawn() {
        harness.setHand(player1, List.of(new VampireSpawn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
    @Test
    @DisplayName("When it enters, each opponent loses 2 life and you gain 2 life")
    void enteringDrainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new VampireSpawn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }
}
