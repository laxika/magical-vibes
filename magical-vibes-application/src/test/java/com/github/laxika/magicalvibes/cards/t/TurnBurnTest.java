package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Turn // Burn is one card whose two halves (and their fusion) are the three modes of a single
 * modal instant, each paying its own total cost.
 */
class TurnBurnTest extends BaseCardTest {

    private static final int TURN = 0;
    private static final int BURN = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Turn makes the target a 0/1 red Weird with no abilities")
    void turnTransformsTarget() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, TURN, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isZero();
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasColor(gd, angel, CardColor.RED)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.WEIRD);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Turn cannot target a player")
    void turnCannotTargetPlayer() {
        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, TURN, playerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Burn deals 2 damage to any target")
    void burnDamagesAnyTarget() {
        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, BURN, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Burn is castable off red mana alone — the mode's {1}{R} replaces the printed {2}{U}")
    void burnIsPaidWithItsOwnCost() {
        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, BURN, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fuse resolves Turn then Burn on independent targets")
    void fuseUsesIndependentTargets() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of(angel.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isZero();
        assertThat(angel.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.WEIRD);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fuse may put both halves on the same creature")
    void fuseAllowsSharedTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        // Turn makes it 0/1, then Burn deals 2 — lethal.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = bears.getId();
        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(bearsId, playerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Turn's transformation wears off at end of turn")
    void turnWearsOff() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new TurnBurn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, TURN, angel.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.RED)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isNull();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }
}
