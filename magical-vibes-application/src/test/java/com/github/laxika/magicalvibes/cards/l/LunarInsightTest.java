package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LunarInsightTest extends BaseCardTest {

    private void castLunarInsight() {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws one card for each distinct mana value among controlled nonland permanents")
    void drawsForDistinctManaValues() {
        harness.setHand(player1, new ArrayList<>(List.of(new LunarInsight())));
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new Island());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Counts only nonland permanents controlled by the caster")
    void ignoresLandsAndOpponentsPermanents() {
        harness.setHand(player1, new ArrayList<>(List.of(new LunarInsight())));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new SerraAngel());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }
}
