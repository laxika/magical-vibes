package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BrokersVeteran;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReservoirKraken.class, BrokersVeteran.class, Murder.class})
class ReservoirKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Declining leaves the Kraken and the opponent's creature untapped")
    void decliningDoesNothing() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(kraken.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("Accepting taps the chosen creature and creates one unblockable Fish")
    void acceptingTapsCreatureAndCreatesFish() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(kraken.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        Permanent fish = findPermanent(player1, "Fish");
        assertThat(gqs.getEffectivePower(gd, fish)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fish)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, fish)).isTrue();
    }

    @Test
    @DisplayName("Choosing among several creatures taps only the selected one")
    void choosesCreatureToTap() {
        harness.addToBattlefield(player1, new ReservoirKraken());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger works during an opponent's combat")
    void triggersDuringOpponentsCombat() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());

        resolveBeginningOfCombat(player2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(kraken.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Kraken does not trigger")
    void tappedKrakenDoesNotTrigger() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        kraken.tap();
        harness.addToBattlefield(player2, new BrokersVeteran());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Kraken puts no ability on the stack at the beginning of combat")
    void tappedKrakenDoesNotPutAnAbilityOnTheStack() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        kraken.tap();
        harness.addToBattlefield(player2, new BrokersVeteran());

        enterBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Kraken tapped while its ability waits on the stack does nothing as it resolves")
    void krakenTappedInResponseDoesNothing() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());

        enterBeginningOfCombat(player1);
        kraken.tap();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("With no untapped opposing creature nobody is asked and the Kraken stays untapped")
    void noUntappedOpposingCreatureLeavesKrakenUntapped() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());
        creature.tap();

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(kraken.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("With no opposing creatures nobody is asked and the Kraken stays untapped")
    void noOpposingCreaturesLeavesKrakenUntapped() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());

        resolveBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(kraken.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Fish")).isEmpty();
    }

    @Test
    @CardUsed({ReservoirKraken.class, BrokersVeteran.class, Murder.class})
    @DisplayName("Destroyed while untapped in response, its ability still resolves and a tapping opponent still gives a Fish")
    void untappedKrakenDestroyedInResponseStillCreatesFish() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());
        harness.setHand(player1, List.of(new Murder()));

        enterBeginningOfCombat(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, kraken.getId());
        harness.assertInGraveyard(player1, "Reservoir Kraken");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
        assertThat(findPermanents(player2, "Fish")).isEmpty();
    }

    @Test
    @DisplayName("Each opponent chooses in turn, the chosen creatures are tapped together and only one Fish is created")
    void opponentsChooseInTurnThenTapTogetherForOneFish() {
        Player player3 = addThirdPlayer();
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ReservoirKraken());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());
        Permanent second = harness.addToBattlefieldAndReturn(player3, new BrokersVeteran());

        resolveBeginningOfCombat(player1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player3.getId());
        assertThat(first.isTapped()).isFalse();
        assertThat(kraken.isTapped()).isFalse();

        harness.handleMayAbilityChosen(player3, true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(kraken.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Fish")).hasSize(1);
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }

    private void enterBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        enterBeginningOfCombat(activePlayer);
        harness.passBothPriorities();
    }
}
