package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TidebinderMageTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    class EnterTheBattlefield {

        @Test
        @DisplayName("Taps a green creature an opponent controls and applies the untap lock")
        void tapsGreenCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            castMage(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
        }

        @Test
        @DisplayName("Taps a red creature an opponent controls")
        void tapsRedCreature() {
            harness.addToBattlefield(player2, new HillGiant());
            Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();

            castMage(player2, "Hill Giant");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(giant.isTapped()).isTrue();
            assertThat(giant.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Untap lock lifecycle")
    class UntapLock {

        @Test
        @DisplayName("Locked creature does not untap while Tidebinder Mage remains on the battlefield")
        void lockedCreatureStaysTapped() {
            Permanent mage = addMage(player1);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            bears.tap();
            bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(mage.getId());

            advanceToNextTurn(player1); // advance to player2's untap step

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Locked creature untaps once Tidebinder Mage leaves the battlefield")
        void lockedCreatureUntapsWhenMageRemoved() {
            Permanent mage = addMage(player1);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            bears.tap();
            bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(mage.getId());

            gd.playerBattlefields.get(player1.getId()).remove(mage);

            advanceToNextTurn(player1); // advance to player2's untap step

            assertThat(bears.isTapped()).isFalse();
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target a creature that is neither red nor green")
        void cannotTargetBlueCreature() {
            harness.addToBattlefield(player2, new AirElemental());
            UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
            prepareMageInHand();

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, elementalId, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target own green creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
            prepareMageInHand();

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void prepareMageInHand() {
        harness.setHand(player1, List.of(new TidebinderMage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

    private void castMage(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        prepareMageInHand();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Permanent addMage(Player player) {
        Permanent mage = new Permanent(new TidebinderMage());
        gd.playerBattlefields.get(player.getId()).add(mage);
        return mage;
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
