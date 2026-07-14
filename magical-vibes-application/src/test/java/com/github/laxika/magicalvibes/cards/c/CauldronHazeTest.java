package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CauldronHazeTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void giveMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent findOnBattlefield(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Granted persist returns a killed creature with a -1/-1 counter")
    void grantedPersistReturnsKilledCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CauldronHaze()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        resolveUntilInputOrEmpty();

        Permanent returned = findOnBattlefield("Grizzly Bears");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Any number of target creatures each gain persist")
    void grantsPersistToMultipleCreatures() {
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CauldronHaze()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears1.getId(), bears2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears1, Keyword.PERSIST)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears2, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Granted persist wears off at end of turn")
    void persistWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CauldronHaze()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();

        // Simulate end-of-turn cleanup (CR 514.2 / floating-effect expiry).
        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Can only target creatures")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new CauldronHaze()));
        giveMana();

        UUID mountainId = harness.getPermanentId(player1, "Mountain");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
