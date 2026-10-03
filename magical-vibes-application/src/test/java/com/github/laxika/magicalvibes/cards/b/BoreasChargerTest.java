package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.w.WormsOfTheEarth;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoreasCharger.class, Forest.class, LightningBolt.class, Plains.class,
        PsychogenicProbe.class, WormsOfTheEarth.class, Unsummon.class})
class BoreasChargerTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Chooses an opponent and searches the land-count difference")
    void choosesOpponentAndSearchesLandDifference() {
        harness.setHand(player1, List.of());
        addThirdPlayer();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player3, new Forest());
        harness.addToBattlefield(player3, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());

        destroyWithLightningBolt(charger);

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.validIds()).containsExactly(player2.getId(), player3.getId());

        harness.handlePermanentChosen(player1, player3.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2).allMatch(card -> card instanceof Plains);
        assertThat(search.params().destination()).isEqualTo(
                com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().followUp().basicLandToHand().count()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Plains).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("Still shuffles when no opponent controls more lands")
    void shufflesWithoutEligibleOpponent() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());
        harness.addToBattlefield(player2, new PsychogenicProbe());

        destroyWithLightningBolt(charger);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Plains).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Shuffles an empty library when an opponent has more lands")
    void shufflesEmptyLibrary() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());

        destroyWithLightningBolt(charger);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Can find fewer Plains than the difference and stop the hand search")
    void canFindFewerPlains() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Forest()));
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());

        destroyWithLightningBolt(charger);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .matches(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can fail to find any Plains and still shuffle exactly once")
    void canFailToFindAllPlains() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());

        destroyWithLightningBolt(charger);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    private void destroyWithLightningBolt(Permanent target) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Triggers on returning to hand and puts all remaining found Plains into hand")
    void triggersOnBounceAndFindsThreePlains() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Forest()));
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, charger.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Plains).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof BoreasCharger).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .matches(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Still finds Plains for hand when lands cannot enter the battlefield")
    void putsRemainingPlainsInHandWhenBattlefieldEntryIsForbidden() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new WormsOfTheEarth());
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new BoreasCharger());

        destroyWithLightningBolt(charger);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Plains.class);
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
