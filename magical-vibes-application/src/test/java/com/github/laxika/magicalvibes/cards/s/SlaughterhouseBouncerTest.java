package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Ulcerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlaughterhouseBouncer.class, GrizzlyBears.class, Ulcerate.class})
class SlaughterhouseBouncerTest extends BaseCardTest {

    @Test
    @DisplayName("When Slaughterhouse Bouncer dies with an empty hand, target creature gets -3/-3")
    void deathTriggerShrinksTargetWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addFourFourTarget();

        destroyBouncer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The death trigger does not trigger while its controller has cards in hand")
    void deathTriggerDoesNotTriggerWithCardsInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addFourFourTarget();

        destroyBouncer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The death trigger rechecks the empty-hand condition on resolution")
    void deathTriggerRechecksEmptyHandOnResolution() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addFourFourTarget();

        destroyBouncer();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The -3/-3 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addFourFourTarget();

        destroyBouncer();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The -3/-3 effect kills a 3/3 creature")
    void debuffKillsThreeThreeCreature() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(3);
        targetCard.setToughness(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        destroyBouncer();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addFourFourTarget() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        return harness.addToBattlefieldAndReturn(player2, targetCard);
    }

    private void destroyBouncer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ulcerate()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        UUID bouncerId = harness.getPermanentId(player1, "Slaughterhouse Bouncer");
        harness.castInstant(player2, 0, bouncerId);
        harness.passBothPriorities();
    }
}
