package com.github.laxika.magicalvibes.cards.b;

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

class BloodScrivenerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing with an empty hand draws two cards and loses 1 life")
    void emptyHandDrawDrawsTwoAndLosesLife() {
        harness.addToBattlefield(player1, new BloodScrivener());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Drawing with cards in hand is a normal single draw with no life loss")
    void nonEmptyHandDrawIsUnchanged() {
        harness.addToBattlefield(player1, new BloodScrivener());
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears()
        )));
        harness.setHand(player1, List.of(new Peek(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("An opponent's empty-hand draw is not replaced")
    void doesNotAffectOpponentDraw() {
        harness.addToBattlefield(player1, new BloodScrivener());
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears()
        )));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int startingLife = gd.getLife(player2.getId());

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }
}
