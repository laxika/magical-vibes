package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NikaraLairScavenger.class, GrizzlyBears.class})
class NikaraLairScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Yannik")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card yannik = namedCard("Yannik, Scavenging Sentinel");
        harness.setLibrary(player2, List.of(yannik));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new NikaraLairScavenger());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(yannik);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws and loses life when another creature with a counter leaves")
    void drawsAndLosesLifeForCreatureWithCounter() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NikaraLairScavenger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when another creature leaves without counters")
    void doesNotTriggerForCreatureWithoutCounter() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NikaraLairScavenger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
