package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.m.MantisRider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReachOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a multicolored creature")
    void destroysMulticoloredCreature() {
        harness.addToBattlefield(player2, new MantisRider());
        UUID targetId = harness.getPermanentId(player2, "Mantis Rider");

        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mantis Rider");
        harness.assertInGraveyard(player2, "Mantis Rider");
    }

    @Test
    @DisplayName("Cannot target a colorless creature")
    void cannotTargetColorlessCreature() {
        harness.addToBattlefield(player2, new BronzeSable());
        UUID targetId = harness.getPermanentId(player2, "Bronze Sable");

        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one or more colors");
        harness.assertOnBattlefield(player2, "Bronze Sable");
    }
}
