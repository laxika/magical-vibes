package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostLynx.class, RuneclawBear.class})
class FrostLynxTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({FrostLynx.class, RuneclawBear.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Taps target creature and makes it skip its controller's next untap step")
        void tapsTargetCreatureAndSkipsNextUntap() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

            castFrostLynx(player2, "Runeclaw Bear");
            resolveAllTriggers();

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Frost Lynx enters the battlefield")
        void frostLynxEntersBattlefield() {
            harness.addToBattlefield(player2, new RuneclawBear());
            castFrostLynx(player2, "Runeclaw Bear");
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Frost Lynx");
        }
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID ownBearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.setHand(player1, List.of(new FrostLynx()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventsOnlyTheTargetsControllersNextUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        castFrostLynx(player2, "Runeclaw Bear");
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureStillSkipsNextUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.tap();
        castFrostLynx(player2, "Runeclaw Bear");
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void overlappingTriggersDoNotSkipTwoUntapSteps() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        castFrostLynx(player2, "Runeclaw Bear");
        resolveAllTriggers();
        castFrostLynx(player2, "Runeclaw Bear");
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void entersWithoutAnOpponentCreatureToTarget() {
        harness.setHand(player1, List.of(new FrostLynx()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Frost Lynx");
        assertThat(gd.stack).isEmpty();
    }

    private void castFrostLynx(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new FrostLynx()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
