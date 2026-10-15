package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeguilerOfWills;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DungeonGeists.class, GrizzlyBears.class, BeguilerOfWills.class})
class DungeonGeistsTest extends BaseCardTest {

    @CardUsed({DungeonGeists.class, GrizzlyBears.class})
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

    @CardUsed({DungeonGeists.class, GrizzlyBears.class})
    @Nested
    @DisplayName("Untap lock lifecycle")
    class UntapLock {

        @Test
        @DisplayName("Locked creature does not untap while Dungeon Geists is on the battlefield")
        void lockedCreatureStaysTapped() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            castGeists(player2, "Grizzly Bears");
            harness.passBothPriorities();
            harness.passBothPriorities();

            advanceToNextTurn(player1); // advance to player2's untap step

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Locked creature untaps once Dungeon Geists leaves the battlefield")
        void lockedCreatureUntapsWhenGeistsRemoved() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            castGeists(player2, "Grizzly Bears");
            harness.passBothPriorities();
            harness.passBothPriorities();

            gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Dungeon Geists"));

            advanceToNextTurn(player1); // advance to player2's untap step

            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Lock persists across multiple turns while Dungeon Geists remains")
        void lockPersistsAcrossTurns() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            castGeists(player2, "Grizzly Bears");
            harness.passBothPriorities();
            harness.passBothPriorities();

            advanceToNextTurn(player1); // player2's untap step
            assertThat(bears.isTapped()).isTrue();

            advanceToNextTurn(player2); // player1's untap step

            advanceToNextTurn(player1); // player2's untap step again
            assertThat(bears.isTapped()).isTrue();
        }
    }

    @CardUsed({DungeonGeists.class, GrizzlyBears.class})
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

    private void castGeists(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new DungeonGeists()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.performUntapStep(currentActivePlayer == player1 ? player2 : player1);
    }

    private Permanent prepareControlChange() {
        Permanent target = addCreatureReady(player2, new DungeonGeists());
        addCreatureReady(player2, new BeguilerOfWills());
        addCreatureReady(player2, new BeguilerOfWills());
        castGeists(player2, "Dungeon Geists");
        harness.passBothPriorities();
        return target;
    }

    private void stealGeists() {
        UUID sourceId = harness.getPermanentId(player1, "Dungeon Geists");
        harness.activateAbility(player2, 1, null, sourceId);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(sourceId));
    }

    @Test
    void losingControlEndsUntapPrevention() {
        Permanent target = prepareControlChange();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        stealGeists();

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void losingControlBeforeTriggerResolvesStillTapsButDoesNotLock() {
        Permanent target = prepareControlChange();
        stealGeists();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void regainingControlDoesNotRestoreExpiredLock() {
        Permanent target = prepareControlChange();
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Dungeon Geists");
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new BeguilerOfWills());
        stealGeists();
        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedTargetRemainsLocked() {
        Permanent target = addCreatureReady(player2, new DungeonGeists());
        target.tap();
        castGeists(player2, "Dungeon Geists");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
    }
}
