package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VedalkenArchmageTest extends BaseCardTest {

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Casting an artifact spell draws a card")
    void castingArtifactDrawsCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setHand(player1, List.of(new Spellbook(), new GrizzlyBears()));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not draw a card")
    void castingNonartifactDoesNotDrawCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
