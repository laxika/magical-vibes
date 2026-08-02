package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApocalypseTest extends BaseCardTest {

    private void addCost() {
        // {2}{R}{R}{R}
        harness.addMana(player1, ManaColor.RED, 5);
    }

    @Test
    @DisplayName("Exiles every permanent on both battlefields, lands included")
    void exilesAllPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Apocalypse())));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Caster discards their hand; opponent's hand is untouched")
    void discardsOnlyCastersHand() {
        harness.setHand(player1, new ArrayList<>(List.of(new Apocalypse(), new Shock(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Island())));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves with an empty hand and empty board")
    void resolvesWithNothingToHit() {
        harness.setHand(player1, new ArrayList<>(List.of(new Apocalypse())));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Apocalypse");
    }
}
