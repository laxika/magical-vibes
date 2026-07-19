package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulsMajestyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the target creature's power")
    void drawsEqualToPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, giantId);
        harness.passBothPriorities();

        // Hill Giant is a 3/3: cast one card, draw three.
        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID enemyBearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enemyBearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
