package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PendulumOfPatternsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield causes its controller to gain 3 life")
    void enteringBattlefieldGainsLife() {
        harness.setHand(player1, List.of(new PendulumOfPatterns()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Paying 5 mana and tapping it sacrifices it and draws a card")
    void activatedAbilitySacrificesAndDraws() {
        harness.addToBattlefield(player1, new PendulumOfPatterns());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Pendulum of Patterns");
        harness.assertInGraveyard(player1, "Pendulum of Patterns");
    }
}
