package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.c.CloudspireSkycycle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuidelightMatrix.class, BrightfieldGlider.class, CloudspireSkycycle.class, Forest.class})
class GuidelightMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersAndDrawsCard() {
        harness.setHand(player1, List.of(new GuidelightMatrix()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("The first ability saddles a target Mount until end of turn")
    void saddlesMountUntilEndOfTurn() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new GuidelightMatrix());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, mount.getId());
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(matrix.isTapped()).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("The second ability animates a target Vehicle until end of turn")
    void animatesVehicleUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GuidelightMatrix());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("The abilities can target only your matching Mounts and Vehicles")
    void restrictsTargetsToYourMatchingPermanent() {
        harness.addToBattlefield(player1, new GuidelightMatrix());
        Permanent opponentMount = harness.addToBattlefieldAndReturn(player2, new BrightfieldGlider());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new CloudspireSkycycle());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentMount.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentVehicle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Vehicle ability can be activated during combat")
    void animatesVehicleDuringCombat() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new GuidelightMatrix());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(matrix.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    @DisplayName("The Mount ability cannot be activated during combat")
    void cannotSaddleDuringCombat() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new GuidelightMatrix());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mount.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(matrix.isTapped()).isFalse();
        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("The Vehicle ability can be activated during an opponent's turn")
    void animatesVehicleDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GuidelightMatrix());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    @DisplayName("Both abilities require two mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new GuidelightMatrix());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mount.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(matrix.isTapped()).isFalse();
        assertThat(mount.isSaddled()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("Your Mounts and Vehicles cannot be targeted by the wrong ability")
    void rejectsYourWrongSubtype() {
        harness.addToBattlefield(player1, new GuidelightMatrix());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mount.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
