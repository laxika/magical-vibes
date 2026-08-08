package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OneWithTheMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the greatest mana value among artifacts you control")
    void drawsForGreatestArtifactManaValue() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Non-artifacts and opponent artifacts are ignored")
    void ignoresNonArtifactsAndOpponentArtifacts() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ObeliskOfBant());
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws no cards with no artifacts, and goes to the graveyard")
    void drawsNothingWithoutArtifacts() {
        harness.setHand(player1, List.of(new OneWithTheMachine()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "One with the Machine");
    }
}
