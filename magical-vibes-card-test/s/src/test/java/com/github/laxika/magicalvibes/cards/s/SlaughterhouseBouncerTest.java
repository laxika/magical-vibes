package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RakdosPitDragon;
import com.github.laxika.magicalvibes.cards.s.StalkingVengeance;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
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

@CardUsed({SlaughterhouseBouncer.class, RakdosPitDragon.class, StalkingVengeance.class,
        WreckingBall.class})
class SlaughterhouseBouncerTest extends BaseCardTest {

    @Test
    @DisplayName("When Slaughterhouse Bouncer dies with an empty hand, target creature gets -3/-3")
    void deathTriggerShrinksTargetWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addLargeTarget();

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
        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addLargeTarget();

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
        Permanent target = addLargeTarget();

        destroyBouncer();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new WreckingBall()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The -3/-3 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addLargeTarget();

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosPitDragon());

        destroyBouncer();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rakdos Pit Dragon");
        harness.assertInGraveyard(player2, "Rakdos Pit Dragon");
    }

    @Test
    @DisplayName("The death trigger does not use the stack when no creature can be targeted")
    void deathTriggerDoesNotTriggerWithoutLegalTarget() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());

        destroyBouncer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The death trigger can target a creature its controller controls")
    void deathTriggerCanTargetOwnCreature() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RakdosPitDragon());

        destroyBouncer();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rakdos Pit Dragon");
        harness.assertInGraveyard(player1, "Rakdos Pit Dragon");
    }

    @Test
    @DisplayName("Emptying the hand after death does not create a missed trigger")
    void emptyingHandAfterDeathDoesNotTrigger() {
        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addToBattlefield(player1, new SlaughterhouseBouncer());
        Permanent target = addLargeTarget();

        destroyBouncer();
        harness.setHand(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addLargeTarget() {
        return harness.addToBattlefieldAndReturn(player2, new StalkingVengeance());
    }

    private void destroyBouncer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WreckingBall()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        UUID bouncerId = harness.getPermanentId(player1, "Slaughterhouse Bouncer");
        harness.castAndResolveInstant(player2, 0, bouncerId);
    }
}
