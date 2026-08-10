package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GalvanicKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target artifact")
    void untapsTargetArtifact() {
        harness.addToBattlefield(player1, new GalvanicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an artifact controlled by its controller")
    void canTargetOwnArtifact() {
        harness.addToBattlefield(player1, new GalvanicKey());
        harness.addToBattlefield(player1, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player1, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        harness.addToBattlefield(player1, new GalvanicKey());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Requires three mana to activate")
    void requiresThreeMana() {
        harness.addToBattlefield(player1, new GalvanicKey());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires Galvanic Key to be untapped")
    void requiresUntappedSource() {
        harness.addToBattlefield(player1, new GalvanicKey());
        Permanent key = gd.playerBattlefields.get(player1.getId()).get(0);
        key.tap();
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
