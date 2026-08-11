package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScavengingScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Scavenging Scarab cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent scarab = addCreatureReady(player2, new ScavengingScarab());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
