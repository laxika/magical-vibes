package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.cards.t.ToothyImaginaryFriend;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PirImaginativeRascal.class, GrizzlyBears.class, PyramidOfThePantheon.class,
        TimberlandGuide.class, ToothyImaginaryFriend.class, WalkingBallista.class})
class PirImaginativeRascalTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Toothy")
    void partnerWithSearchesTargetPlayersLibrary() {
        ToothyImaginaryFriend toothy = new ToothyImaginaryFriend();
        harness.setLibrary(player2, List.of(toothy));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new PirImaginativeRascal());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(toothy);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Adds one counter of each kind to permanents you control")
    void addsOneCounterOfEachKindToControlledPermanents() {
        harness.addToBattlefield(player1, new PirImaginativeRascal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add counters to permanents an opponent controls")
    void doesNotAddCountersToOpponentPermanents() {
        harness.addToBattlefield(player1, new PirImaginativeRascal());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, List.of(opponentBears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The target player may decline the partner search")
    void targetPlayerMayDeclineSearch() {
        ToothyImaginaryFriend toothy = new ToothyImaginaryFriend();
        harness.setLibrary(player2, List.of(toothy));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new PirImaginativeRascal());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(toothy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may search their own library and fail to find Toothy")
    void controllerMayFailToFindPartner() {
        ToothyImaginaryFriend toothy = new ToothyImaginaryFriend();
        harness.setLibrary(player1, List.of(toothy));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new PirImaginativeRascal());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(toothy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Adds exactly one to a batch of counters as a permanent enters")
    void addsOneToMultipleCountersOnEntry() {
        harness.addToBattlefield(player1, new PirImaginativeRascal());
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent ballista = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WalkingBallista)
                .findFirst().orElseThrow();
        assertThat(ballista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not create counters when zero counters would be put on a permanent")
    void doesNotReplaceZeroCounters() {
        harness.addToBattlefield(player1, new PirImaginativeRascal());
        harness.setHand(player1, List.of(new WalkingBallista()));

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Ballista");
        harness.assertInGraveyard(player1, "Walking Ballista");
    }

    @Test
    @DisplayName("Adds a counter to Pir itself even when an opponent places the counters")
    void addsCounterToSelfPlacedByOpponent() {
        Permanent pir = harness.addToBattlefieldAndReturn(player1, new PirImaginativeRascal());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0, List.of(pir.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
