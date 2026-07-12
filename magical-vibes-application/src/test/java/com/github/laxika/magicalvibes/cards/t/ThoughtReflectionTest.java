package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("A single draw draws two cards instead for the controller")
    void doublesControllerDraw() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Island");
    }

    @Test
    @DisplayName("Only the controller's draws are doubled, not an opponent's")
    void doesNotDoubleOpponentDraw() {
        harness.addToBattlefield(player1, new ThoughtReflection());
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears()
        )));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }
}
