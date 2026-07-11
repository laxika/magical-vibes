package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheftOfDreamsTest extends BaseCardTest {

    private void castTheftOfDreams() {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addTapped(GrizzlyBears bears) {
        Permanent perm = harness.addToBattlefieldAndReturn(player2, bears);
        perm.tap();
        return perm;
    }

    @Test
    @DisplayName("Draws one card per tapped creature the opponent controls")
    void drawsPerTappedCreature() {
        harness.setHand(player1, new ArrayList<>(List.of(new TheftOfDreams())));
        addTapped(new GrizzlyBears());
        addTapped(new GrizzlyBears());
        // Untapped creature is not counted.
        harness.addToBattlefield(player2, new GoblinPiker());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castTheftOfDreams();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Only the opponent's tapped creatures are counted, not the caster's")
    void ignoresCastersTappedCreatures() {
        harness.setHand(player1, new ArrayList<>(List.of(new TheftOfDreams())));
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        own.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castTheftOfDreams();

        // Opponent controls no tapped creatures -> draw nothing.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Tapped non-creature permanents are not counted")
    void ignoresTappedNonCreatures() {
        harness.setHand(player1, new ArrayList<>(List.of(new TheftOfDreams())));
        addTapped(new GrizzlyBears());
        // A tapped land is not a creature and must not be counted.
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castTheftOfDreams();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new TheftOfDreams())));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
