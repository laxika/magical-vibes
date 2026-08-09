package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CovetousDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when its controller controls no artifacts")
    void sacrificesWhenNoArtifacts() {
        castDragon();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Covetous Dragon");
        harness.assertInGraveyard(player1, "Covetous Dragon");
    }

    @Test
    @DisplayName("Survives while its controller controls an artifact")
    void survivesWithArtifact() {
        harness.addToBattlefield(player1, new Spellbook());
        castDragon();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Covetous Dragon");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("An opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new Spellbook());
        castDragon();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Covetous Dragon");
        harness.assertInGraveyard(player1, "Covetous Dragon");
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    private void castDragon() {
        harness.setHand(player1, List.of(new CovetousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }
}
