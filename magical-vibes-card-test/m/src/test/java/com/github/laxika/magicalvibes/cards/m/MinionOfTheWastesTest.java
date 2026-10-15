package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinionOfTheWastes.class, HornedTurtle.class, PlatinumEmperion.class})
class MinionOfTheWastesTest extends BaseCardTest {

    private void cast(String lifePaid) {
        harness.castFromHand(player1, new MinionOfTheWastes(), "{3}{B}{B}{B}");
        harness.passBothPriorities();
        if (lifePaid != null) {
            harness.handleListChoice(player1, lifePaid);
        }
    }

    @Test
    @DisplayName("Entering awaits a life payment choice")
    void enteringAwaitsLifePayment() {
        cast(null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.PayAnyAmountOfLifeAsEnters.class);
    }

    @Test
    @DisplayName("Paying 5 life makes it a 5/5 and costs 5 life")
    void payingFiveLife() {
        harness.setLife(player1, 20);

        cast("5");

        Permanent minion = findPermanent(player1, "Minion of the Wastes");
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Paying 0 life leaves a 0/0 that dies to state-based actions")
    void payingZeroLifeDies() {
        harness.setLife(player1, 20);

        cast("0");

        harness.assertNotOnBattlefield(player1, "Minion of the Wastes");
        harness.assertInGraveyard(player1, "Minion of the Wastes");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot pay more life than the controller has")
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 4);

        cast(null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0", "1", "2", "3", "4");
    }

    @Test
    @DisplayName("Later life-total changes do not change the amount paid on entry")
    void powerAndToughnessRemainEqualToOriginalPayment() {
        cast("5");
        Permanent minion = findPermanent(player1, "Minion of the Wastes");

        harness.setLife(player1, 30);
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(5);

        harness.setLife(player1, 2);
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each Minion remembers its own life payment")
    void multipleMinionsRememberSeparatePayments() {
        cast("3");
        Permanent first = findPermanent(player1, "Minion of the Wastes");
        cast("7");
        Permanent second = findPermanents(player1, "Minion of the Wastes").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();

        harness.assertLife(player1, 10);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(7);
    }

    @Test
    @DisplayName("A controller whose life total cannot change may pay only zero")
    void cannotPayPositiveLifeWithPlatinumEmperion() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        cast(null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0");

        harness.handleListChoice(player1, "0");
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Minion of the Wastes");
        harness.assertInGraveyard(player1, "Minion of the Wastes");
    }

    @Test
    @DisplayName("An opponent's Platinum Emperion does not restrict the controller's payment")
    void opponentLifeRestrictionDoesNotPreventPayment() {
        harness.addToBattlefield(player2, new PlatinumEmperion());

        cast("5");

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        Permanent minion = findPermanent(player1, "Minion of the Wastes");
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(5);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage after lethal damage to its blocker")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);

        cast("5");
        Permanent minion = findPermanent(player1, "Minion of the Wastes");
        minion.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new HornedTurtle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player2, "Horned Turtle");
        harness.assertOnBattlefield(player1, "Minion of the Wastes");
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(5);
    }
}
