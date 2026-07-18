package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AliBabaTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: Tap target Wall taps the targeted Wall")
    void tapsTargetWall() {
        harness.addToBattlefield(player1, new AliBaba());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.addToBattlefield(player1, new AliBaba());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wall");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new AliBaba());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
