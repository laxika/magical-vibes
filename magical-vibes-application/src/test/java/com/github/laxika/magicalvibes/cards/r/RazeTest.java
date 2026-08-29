package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a land and destroys target land")
    void sacrificesLandAndDestroysTargetLand() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Raze()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, targetLand.getId(), sacrificedLand.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot cast without a land to sacrifice")
    void cannotCastWithoutLandToSacrifice() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Raze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, targetLand.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Raze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, targetCreature.getId(), sacrificedLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
