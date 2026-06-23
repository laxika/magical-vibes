package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DungeonGeistsTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("ETB trigger goes on the stack when Dungeon Geists enters")
        void etbTriggerGoesOnStack() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castGeists(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dungeon Geists");
        }

        @Test
        @DisplayName("Taps target creature an opponent controls and applies untap lock")
        void tapsTargetAndAppliesLock() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();

            castGeists(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
        }

        @Test
        @DisplayName("Dungeon Geists enters the battlefield")
        void geistsEntersBattlefield() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castGeists(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Dungeon Geists");
        }
    }

    @Nested
    @DisplayName("Untap lock lifecycle")
    class UntapLock {

        @Test
        @DisplayName("Locked creature does not untap while Dungeon Geists is on the battlefield")
        void lockedCreatureStaysTapped() {
            Permanent geists = addGeists(player1);
            Permanent bears = addReadyCreature(player2);

            bears.tap();
            bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(geists.getId());

            advanceToNextTurn(player1); // advance to player2's untap step

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Locked creature untaps once Dungeon Geists leaves the battlefield")
        void lockedCreatureUntapsWhenGeistsRemoved() {
            Permanent geists = addGeists(player1);
            Permanent bears = addReadyCreature(player2);

            bears.tap();
            bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(geists.getId());

            gd.playerBattlefields.get(player1.getId()).remove(geists);

            advanceToNextTurn(player1); // advance to player2's untap step

            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Lock persists across multiple turns while Dungeon Geists remains")
        void lockPersistsAcrossTurns() {
            Permanent geists = addGeists(player1);
            Permanent bears = addReadyCreature(player2);

            bears.tap();
            bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(geists.getId());

            advanceToNextTurn(player1); // player2's untap step
            assertThat(bears.isTapped()).isTrue();

            advanceToNextTurn(player2); // player1's untap step

            advanceToNextTurn(player1); // player2's untap step again
            assertThat(bears.isTapped()).isTrue();
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.setHand(player1, List.of(new DungeonGeists()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Helpers =====

    private void castGeists(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new DungeonGeists()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Permanent addGeists(Player player) {
        Permanent geists = new Permanent(new DungeonGeists());
        gd.playerBattlefields.get(player.getId()).add(geists);
        return geists;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
