package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunAfoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an opponent's flying creature")
    void sacrificesFlyingCreature() {
        harness.addToBattlefield(player2, new CloudSprite());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castRunAfoul();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloud Sprite");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cloud Sprite");
    }

    @Test
    @DisplayName("Opponent chooses among multiple flying creatures")
    void opponentChoosesFlyingCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        castRunAfoul();

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(second.getId())
                .doesNotContain(first.getId());
    }

    @Test
    @DisplayName("Does nothing when the opponent controls no flying creature")
    void noFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castRunAfoul();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Run Afoul");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RunAfoul()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castRunAfoul() {
        harness.setHand(player1, List.of(new RunAfoul()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
