package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RejoinTheFight.class, GrizzlyBears.class, Island.class})
class RejoinTheFightTest extends BaseCardTest {

    @Test
    void millsThreeThenOpponentChoosesCreatureToReturnUnderControllerControl() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RejoinTheFight()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(creature);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Island).hasSize(3);
    }

    @Test
    void eachOpponentChoosesAUniqueRemainingCreature() {
        Player player3 = addThirdPlayer();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RejoinTheFight()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, 0);

        PendingInteraction.GraveyardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player3.getId());
        assertThat(secondChoice.cardPool()).containsExactly(second);
        harness.handleGraveyardCardChosen(player3, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player3.getId())).isEmpty();
    }

    @Test
    void doesNotOfferNoncreatureCards() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new RejoinTheFight()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Island).hasSize(4);
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
