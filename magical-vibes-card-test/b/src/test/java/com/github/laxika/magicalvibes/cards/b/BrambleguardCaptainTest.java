package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrambleguardCaptain.class, GrizzlyBears.class, HillGiant.class, MightOfTheMeek.class})
class BrambleguardCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature +X/+0 where X is Brambleguard Captain's power")
    void boostsTargetBySourcePower() {
        addCaptain();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Targets only creatures you control, including itself")
    void targetsOnlyControlledCreatures() {
        Permanent captain = addCaptain();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(captain.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCaptain();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addCaptain();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can boost itself without repeatedly recalculating the boost")
    void boostsItselfByItsPowerBeforeTheBoost() {
        Permanent captain = addCaptain();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, captain.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Uses its power when the ability resolves, including a response spell's boost")
    void usesPowerAtResolution() {
        Permanent captain = addCaptain();
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new BrambleguardCaptain()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, captain.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, captain.getId());

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
    }

    @Test
    @DisplayName("Negative source power gives a zero boost")
    void negativeSourcePowerDoesNotReduceItsOwnPower() {
        Permanent captain = addCaptain();
        captain.setPowerModifier(-3);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, captain.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
    }

    private Permanent addCaptain() {
        return harness.addToBattlefieldAndReturn(player1, new BrambleguardCaptain());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
