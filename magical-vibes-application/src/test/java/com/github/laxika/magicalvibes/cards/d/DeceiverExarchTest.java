package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeceiverExarch.class, Island.class, SpinedThopter.class})
class DeceiverExarchTest extends BaseCardTest {

    @CardUsed({DeceiverExarch.class, Island.class})
    @Nested
    @DisplayName("Mode 1: Untap target permanent you control")
    class UntapMode {

        @Test
        @DisplayName("Untaps a tapped permanent you control")
        void untapsTappedPermanent() {
            Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
            island.tap();
            UUID targetId = island.getId();

            castWithUntapMode(targetId);
            resolveAllTriggers();

            assertThat(island.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Deceiver Exarch enters the battlefield when choosing untap mode")
        void exarchEntersBattlefield() {
            UUID targetId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();

            castWithUntapMode(targetId);

            harness.assertOnBattlefield(player1, "Deceiver Exarch");
        }
    }

    @CardUsed({DeceiverExarch.class, SpinedThopter.class})
    @Nested
    @DisplayName("Mode 2: Tap target permanent an opponent controls")
    class TapMode {

        @Test
        @DisplayName("Taps an untapped permanent an opponent controls")
        void tapsOpponentPermanent() {
            Permanent thopter = harness.addToBattlefieldAndReturn(player2, new SpinedThopter());
            assertThat(thopter.isTapped()).isFalse();
            UUID targetId = thopter.getId();

            castWithTapMode(targetId);
            resolveAllTriggers();

            assertThat(thopter.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Deceiver Exarch enters the battlefield when choosing tap mode")
        void exarchEntersBattlefield() {
            UUID targetId = harness.addToBattlefieldAndReturn(player2, new SpinedThopter()).getId();

            castWithTapMode(targetId);

            harness.assertOnBattlefield(player1, "Deceiver Exarch");
        }
    }

    @Test
    void canChooseUntapModeWhenEnteringWithoutBeingCast() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        harness.enterBattlefieldAndReturn(player1, new DeceiverExarch());
        harness.passPriority(player1);
        chooseMode("Untap target permanent you control");
        harness.handlePermanentChosen(player1, island.getId());
        resolveAllTriggers();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    void canChooseTapModeWhenEnteringWithoutBeingCast() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.enterBattlefieldAndReturn(player1, new DeceiverExarch());
        harness.passPriority(player1);
        chooseMode("Tap target permanent an opponent controls");
        harness.handlePermanentChosen(player1, island.getId());
        resolveAllTriggers();

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    void canUntapItselfWhenItIsTheOnlyPermanent() {
        castExarch();
        Permanent exarch = findPermanent(player1, "Deceiver Exarch");
        chooseMode("Untap target permanent you control");
        harness.handlePermanentChosen(player1, exarch.getId());
        exarch.tap();
        resolveAllTriggers();

        assertThat(exarch.isTapped()).isFalse();
    }

    @Test
    void untapModeRejectsOpponentsPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.enterBattlefieldAndReturn(player1, new DeceiverExarch());
        harness.passPriority(player1);
        chooseMode("Untap target permanent you control");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void tapModeRejectsOwnPermanent() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.enterBattlefieldAndReturn(player1, new DeceiverExarch());
        harness.passPriority(player1);
        chooseMode("Tap target permanent an opponent controls");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownIsland.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void canFlashInDuringOpponentsUpkeepAndTapTheirLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castWithTapMode(island.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Deceiver Exarch");
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    void tapTriggerDoesNotAffectTargetThatYouGainControlOf() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        castWithTapMode(island.getId());
        gd.playerBattlefields.get(player2.getId()).remove(island);
        gd.playerBattlefields.get(player1.getId()).add(island);
        resolveAllTriggers();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    void untapTriggerDoesNotAffectTargetThatOpponentGainsControlOf() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        castWithUntapMode(island.getId());
        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerBattlefields.get(player2.getId()).add(island);
        resolveAllTriggers();

        assertThat(island.isTapped()).isTrue();
    }

    private void castWithUntapMode(UUID targetId) {
        castExarch();
        chooseMode("Untap target permanent you control");
        harness.handlePermanentChosen(player1, targetId);
    }

    private void castWithTapMode(UUID targetId) {
        castExarch();
        chooseMode("Tap target permanent an opponent controls");
        harness.handlePermanentChosen(player1, targetId);
    }

    private void castExarch() {
        harness.setHand(player1, List.of(new DeceiverExarch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
    }
}
