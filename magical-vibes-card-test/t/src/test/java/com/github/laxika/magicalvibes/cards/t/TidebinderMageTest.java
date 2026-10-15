package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Domestication;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({TidebinderMage.class, GrizzlyBears.class, HillGiant.class, AirElemental.class, Domestication.class})
class TidebinderMageTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({TidebinderMage.class, GrizzlyBears.class, HillGiant.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Taps a green creature an opponent controls and applies the untap lock")
        void tapsGreenCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            castMage(player2, "Grizzly Bears");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
        }

        @Test
        @DisplayName("Taps a red creature an opponent controls")
        void tapsRedCreature() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            castMage(player2, "Hill Giant");
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(giant.isTapped()).isTrue();
            assertThat(giant.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Untap lock lifecycle")
    @CardUsed({TidebinderMage.class, GrizzlyBears.class})
    class UntapLock {

        @Test
        @DisplayName("Locked creature does not untap while Tidebinder Mage remains on the battlefield")
        void lockedCreatureStaysTapped() {
            Permanent mage = addMage(player1);
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            bears.tap();
            bears.getUntapPreventedWhileSourceOnBattlefieldIds().add(mage.getId());

            harness.performUntapStep(player2);

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

            harness.performUntapStep(player2);

            assertThat(bears.isTapped()).isFalse();
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({TidebinderMage.class, GrizzlyBears.class, AirElemental.class})
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

    @Test
    @DisplayName("An already tapped target remains locked through multiple untap steps")
    void alreadyTappedTargetRemainsLocked() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        castMage(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing control of the Mage ends the resolved untap lock")
    void losingControlEndsLock() {
        Permanent bears = resolveMageAgainstBears();
        Permanent mage = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Tidebinder Mage"));

        stealCreature(player2, mage);
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regaining control of the Mage does not restore its previous untap lock")
    void regainingControlDoesNotRestoreLock() {
        Permanent bears = resolveMageAgainstBears();
        Permanent mage = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Tidebinder Mage"));

        stealCreature(player2, mage);
        stealCreature(player1, mage);
        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taking control of the locked target does not end the lock")
    void gainingControlOfTargetPreservesLock() {
        Permanent bears = resolveMageAgainstBears();

        stealCreature(player1, bears);
        harness.performUntapStep(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target still taps when the Mage leaves before its trigger resolves")
    void mageLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMage(player2, "Grizzly Bears");
        harness.passBothPriorities();
        Permanent mage = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Tidebinder Mage"));
        gd.playerBattlefields.get(player1.getId()).remove(mage);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    private Permanent resolveMageAgainstBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMage(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.isTapped()).isTrue();
        return bears;
    }

    private void stealCreature(Player controller, Permanent creature) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(controller, List.of(new Domestication()));
        harness.addMana(controller, ManaColor.BLUE, 4);
        harness.castEnchantment(controller, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(controller.getId())).contains(creature);
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
        return harness.addToBattlefieldAndReturn(player, new TidebinderMage());
    }
}
