package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.d.DesecrationDemon;
import com.github.laxika.magicalvibes.cards.g.Griselbrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarvesterOfSouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasContractTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws four cards and loses four life")
    void enterDrawsFourAndLosesFour() {
        harness.setHand(player1, List.of(new LilianasContract()));
        harness.setLibrary(player1, List.<Card>of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve the enchantment (queues the ETB trigger)
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Upkeep with four differently named Demons wins the game")
    void winsWithFourDifferentlyNamedDemons() {
        harness.addToBattlefield(player1, new LilianasContract());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        harness.addToBattlefield(player1, new Griselbrand());
        harness.addToBattlefield(player1, new HarvesterOfSouls());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Four Demons sharing a name do not trigger the win")
    void sameNamedDemonsDoNotTrigger() {
        harness.addToBattlefield(player1, new LilianasContract());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DesecrationDemon());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Three differently named Demons are not enough")
    void threeDemonsDoNotTrigger() {
        harness.addToBattlefield(player1, new LilianasContract());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        harness.addToBattlefield(player1, new Griselbrand());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Demons controlled by the opponent do not count")
    void opponentDemonsDoNotCount() {
        harness.addToBattlefield(player1, new LilianasContract());
        harness.addToBattlefield(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        harness.addToBattlefield(player2, new Griselbrand());
        harness.addToBattlefield(player2, new HarvesterOfSouls());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
