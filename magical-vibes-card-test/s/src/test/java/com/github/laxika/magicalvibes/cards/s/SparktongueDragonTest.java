package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparktongueDragon.class, GreenwoodSentinel.class, TitanicGrowth.class})
class SparktongueDragonTest extends BaseCardTest {

    private void castDragon() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new SparktongueDragon(), "{3}{R}{R}");
        harness.passBothPriorities();
    }

    private void offerPayment() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The enter trigger has no target until payment succeeds")
    void enteringDoesNotPromptForTarget() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        castDragon();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        offerPayment();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(harness.getPermanentId(player2, "Greenwood Sentinel"),
                player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Paying {2}{R} creates a separate trigger dealing 3 damage to a creature")
    void payingDealsDamageToCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();

        castDragon();
        offerPayment();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Paying {2}{R} deals 3 damage to the chosen player only after the second trigger resolves")
    void payingDealsDamageToPlayer() {
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castDragon();
        offerPayment();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Declining payment requests no target and creates no damage trigger")
    void decliningDealsNoDamage() {
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castDragon();
        offerPayment();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("The opponent can save the target after payment by responding to the damage trigger")
    void opponentCanRespondAfterPayment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        castDragon();
        offerPayment();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Dragon can target itself after paying")
    void canDealLethalDamageToItself() {
        castDragon();
        offerPayment();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sparktongue Dragon"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sparktongue Dragon");
        harness.assertInGraveyard(player1, "Sparktongue Dragon");
    }
}
