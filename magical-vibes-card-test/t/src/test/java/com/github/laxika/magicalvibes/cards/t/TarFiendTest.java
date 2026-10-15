package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.g.GrixisCharm;
import com.github.laxika.magicalvibes.cards.h.HissingIguanar;
import com.github.laxika.magicalvibes.cards.y.YokedPlowbeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TarFiend.class, CylianElf.class, YokedPlowbeast.class})
class TarFiendTest extends BaseCardTest {

    private void castTarFiend(java.util.UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new TarFiend())));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Devouring two creatures adds twice as many +1/+1 counters and discards that many")
    void devourTwoAddsFourCountersAndDiscardsTwo() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player2, new ArrayList<>(List.of(new CylianElf(), new YokedPlowbeast())));

        castTarFiend(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        // Fodder is gone; Tar Fiend remains with four +1/+1 counters.
        Permanent tarFiend = findPermanent(player1, "Tar Fiend");
        assertThat(tarFiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities(); // resolve discard trigger -> discard choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and discards nothing")
    void devourNoneNoCountersNoDiscard() {
        harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player2, new ArrayList<>(List.of(new CylianElf(), new YokedPlowbeast())));

        castTarFiend(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent tarFiend = findPermanent(player1, "Tar Fiend");
        assertThat(tarFiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve discard trigger (discards 0)

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Entering without any creatures still triggers but discards nothing")
    void noCreaturesToDevour() {
        harness.setHand(player2, List.of(new CylianElf()));

        castTarFiend(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanent(player1, "Tar Fiend").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can be targeted and chooses which card to discard")
    void targetsController() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        castTarFiend(player1.getId());
        harness.setHand(player1, List.of(new CylianElf(), new YokedPlowbeast()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Cylian Elf");
        harness.assertInGraveyard(player1, "Yoked Plowbeast");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Tar Fiend").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A player with fewer cards than creatures devoured discards their entire hand")
    void discardsOnlyAvailableCards() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player2, List.of(new YokedPlowbeast()));
        castTarFiend(player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Yoked Plowbeast");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({HissingIguanar.class})
    @DisplayName("A devoured death watcher sees the other creatures sacrificed simultaneously")
    void devouredWatcherSeesOtherDevouredCreatureDie() {
        Permanent iguanar = harness.addToBattlefieldAndReturn(player1, new HissingIguanar());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player2, List.of());
        castTarFiend(player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(iguanar.getId(), elf.getId()));

        harness.assertInGraveyard(player1, "Hissing Iguanar");
        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanent(player1, "Tar Fiend").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @CardUsed({GrixisCharm.class})
    @DisplayName("The discard trigger remembers devoured creatures after Tar Fiend leaves")
    void discardsAfterSourceLeavesBattlefield() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player2, List.of(new CylianElf(), new YokedPlowbeast()));
        castTarFiend(player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));
        Permanent tarFiend = findPermanent(player1, "Tar Fiend");
        harness.setHand(player1, List.of(new GrixisCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, tarFiend.getId());
        harness.assertNotOnBattlefield(player1, "Tar Fiend");
        harness.assertInHand(player1, "Tar Fiend");

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Cylian Elf");
        harness.assertInGraveyard(player2, "Yoked Plowbeast");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
