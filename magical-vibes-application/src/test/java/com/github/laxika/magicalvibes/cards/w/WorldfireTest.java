package com.github.laxika.magicalvibes.cards.w;

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

class WorldfireTest extends BaseCardTest {

    private void addCost() {
        // {6}{R}{R}{R}
        harness.addMana(player1, ManaColor.RED, 9);
    }

    @Test
    @DisplayName("Exiles every permanent on both battlefields, lands included")
    void exilesAllPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Worldfire()));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles all cards from all hands and graveyards")
    void exilesAllHandsAndGraveyards() {
        harness.setHand(player1, new ArrayList<>(List.of(new Worldfire(), new GrizzlyBears())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Island())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Worldfire itself is put into its owner's graveyard after it finishes resolving,
        // so the caster's graveyard holds exactly that one card.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Worldfire");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each player's life total becomes 1")
    void setsEachPlayerLifeToOne() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new Worldfire()));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(1);
    }
}
