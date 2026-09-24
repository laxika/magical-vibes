package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanicKey.class, AlphaMyr.class, FangrenHunter.class})
class GalvanicKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target artifact")
    void untapsTargetArtifact() {
        harness.addToBattlefield(player1, new GalvanicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an artifact controlled by its controller")
    void canTargetOwnArtifact() {
        harness.addToBattlefield(player1, new GalvanicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        harness.addToBattlefield(player1, new GalvanicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FangrenHunter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Requires three mana to activate")
    void requiresThreeMana() {
        harness.addToBattlefield(player1, new GalvanicKey());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires Galvanic Key to be untapped")
    void requiresUntappedSource() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new GalvanicKey());
        key.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target and untap Galvanic Key itself")
    void canTargetAndUntapItself() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new GalvanicKey());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, key.getId());

        assertThat(key.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(key.isTapped()).isFalse();
    }
}
