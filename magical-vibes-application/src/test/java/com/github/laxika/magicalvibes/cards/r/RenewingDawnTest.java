package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RenewingDawnTest extends BaseCardTest {

    private void castRenewingDawn() {
        harness.setHand(player1, List.of(new RenewingDawn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 2 life for each Mountain the target opponent controls")
    void gainsTwoLifePerMountain() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        castRenewingDawn();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Only Mountains are counted; other lands are ignored")
    void ignoresNonMountains() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        castRenewingDawn();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Only the target opponent's Mountains count, not the caster's")
    void ignoresCastersMountains() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        castRenewingDawn();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains no life when the opponent controls no Mountains")
    void gainsNothingWithoutMountains() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Forest());

        castRenewingDawn();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RenewingDawn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
