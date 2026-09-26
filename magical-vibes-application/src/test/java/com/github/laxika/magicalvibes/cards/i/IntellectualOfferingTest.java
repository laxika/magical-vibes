package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntellectualOffering.class, GrizzlyBears.class, Island.class})
class IntellectualOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Each mode chooses an opponent and draws or untaps their nonlands")
    void drawsAndUntapsWithSoleOpponent() {
        Permanent ownCreature = tappedCreature(player1);
        Permanent ownLand = tappedLand(player1);
        Permanent opponentCreature = tappedCreature(player2);
        Permanent opponentLand = tappedLand(player2);
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new IntellectualOffering()));
        addOfferingMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(3);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The two opponent choices are independent")
    void choosesDifferentOpponentsForDrawAndUntap() {
        Player player3 = addThirdPlayer();
        Permanent ownCreature = tappedCreature(player1);
        Permanent player2Creature = tappedCreature(player2);
        Permanent player3Creature = tappedCreature(player3);
        Permanent player2Land = tappedLand(player2);
        Permanent player3Land = tappedLand(player3);
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new IntellectualOffering()));
        addOfferingMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.validPlayerIds()).containsExactly(player2.getId(), player3.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(3);
        assertThat(gd.playerHands.get(player3.getId())).isEmpty();

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.validPlayerIds()).containsExactly(player2.getId(), player3.getId());

        harness.handlePermanentChosen(player1, player3.getId());

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(player3Creature.isTapped()).isFalse();
        assertThat(player2Creature.isTapped()).isTrue();
        assertThat(player2Land.isTapped()).isTrue();
        assertThat(player3Land.isTapped()).isTrue();
    }

    private void addOfferingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent tappedCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.tap();
        return permanent;
    }

    private Permanent tappedLand(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Island());
        permanent.tap();
        return permanent;
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
}
