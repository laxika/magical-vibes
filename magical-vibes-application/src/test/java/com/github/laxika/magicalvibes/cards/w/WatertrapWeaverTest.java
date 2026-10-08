package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DeeprootChampion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WatertrapWeaver.class, DeeprootChampion.class})
class WatertrapWeaverTest extends BaseCardTest {

    @CardUsed({WatertrapWeaver.class, DeeprootChampion.class})
    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("ETB trigger goes on the stack when Watertrap Weaver enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new DeeprootChampion());
            castWeaver(player2, "Deeproot Champion");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Watertrap Weaver");
        }

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());
            assertThat(bears.isTapped()).isFalse();

            castWeaver(player2, "Deeproot Champion");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature doesn't untap during its controller's next untap step")
        void targetSkipsNextUntap() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());

            castWeaver(player2, "Deeproot Champion");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Watertrap Weaver enters the battlefield")
        void weaverEntersBattlefield() {
            harness.addToBattlefield(player2, new DeeprootChampion());
            castWeaver(player2, "Deeproot Champion");
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Watertrap Weaver");
        }
    }

    @CardUsed({WatertrapWeaver.class, DeeprootChampion.class})
    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new DeeprootChampion());
            UUID ownBearId = harness.getPermanentId(player1, "Deeproot Champion");
            harness.setHand(player1, List.of(new WatertrapWeaver()));
            harness.addMana(player1, ManaColor.BLUE, 3);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("The lock expires after the opponent's next untap step")
    void lockLastsForExactlyOneControllerUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());
        castWeaver(player2, "Deeproot Champion");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature still skips its next untap step")
    void alreadyTappedTargetStillGetsLocked() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());
        target.setTapped(true);
        castWeaver(player2, "Deeproot Champion");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two Weaver triggers before the same untap step do not extend the lock")
    void multipleLocksExpireDuringTheSameUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());
        castWeaver(player2, "Deeproot Champion");
        harness.passBothPriorities();
        harness.passBothPriorities();
        castWeaver(player2, "Deeproot Champion");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves after Weaver leaves the battlefield")
    void triggerDoesNotDependOnItsSourceRemaining() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());
        castWeaver(player2, "Deeproot Champion");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A target that becomes controlled by Weaver's controller is illegal on resolution")
    void targetChangingToOwnControlMakesTriggerFail() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeeprootChampion());
        castWeaver(player2, "Deeproot Champion");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        target.setTapped(true);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Weaver can enter with no opposing creatures")
    void entersWithNoLegalTargets() {
        harness.castFromHand(player1, new WatertrapWeaver(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Watertrap Weaver");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castWeaver(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new WatertrapWeaver()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
