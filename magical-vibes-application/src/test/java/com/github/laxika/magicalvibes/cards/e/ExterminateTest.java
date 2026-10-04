package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DalekDrone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exterminate.class, DalekDrone.class, GrizzlyBears.class})
class ExterminateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and its controller loses 3 life")
    void destroysCreatureAndItsControllerLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        castExterminate(target, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Tapping Daleks for replicate creates one copy per tapped Dalek")
    void tappingDaleksCreatesReplicateCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent dalek1 = harness.addToBattlefieldAndReturn(player1, new DalekDrone());
        Permanent dalek2 = harness.addToBattlefieldAndReturn(player1, new DalekDrone());
        harness.setLife(player2, 20);
        castExterminate(target, List.of(dalek1.getId(), dalek2.getId()));

        assertThat(dalek1.isTapped()).isTrue();
        assertThat(dalek2.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Replicate can only tap Daleks you control")
    void replicateRequiresControlledDaleks() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent nonDalek = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThatThrownBy(() -> castExterminate(target, List.of(nonDalek.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void replicateCopyCanChooseAnotherCreature() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        Permanent dalek = harness.addToBattlefieldAndReturn(player1, new DalekDrone());
        harness.setLife(player2, 20);
        castExterminate(originalTarget, List.of(dalek.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        assertThat(dalek.isTapped()).isTrue();
    }

    @Test
    void canDestroyOwnCreatureAndLoseLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DalekDrone());
        harness.setLife(player1, 20);
        castExterminate(target, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dalek Drone");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerStillLosesLifeWhenCreatureRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        target.setRegenerationShield(1);
        harness.setLife(player2, 20);
        castExterminate(target, List.of());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dalek Drone");
        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    void illegalTargetDoesNotCauseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        harness.setLife(player2, 20);
        castExterminate(target, List.of());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayReplicateWithOpponentsDalek() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DalekDrone());

        assertThatThrownBy(() -> castExterminate(target, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotPayReplicateWithTappedDalek() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        Permanent dalek = harness.addToBattlefieldAndReturn(player1, new DalekDrone());
        dalek.setTapped(true);

        assertThatThrownBy(() -> castExterminate(target, List.of(dalek.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayReplicateTwiceWithSameDalek() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DalekDrone());
        Permanent dalek = harness.addToBattlefieldAndReturn(player1, new DalekDrone());

        assertThatThrownBy(() -> castExterminate(target, List.of(dalek.getId(), dalek.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dalek.isTapped()).isFalse();
    }

    private void castExterminate(Permanent target, List<UUID> dalekIds) {
        harness.setHand(player1, List.of(new Exterminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryTappingPermanents(player1, 0, target.getId(), dalekIds);
    }
}
