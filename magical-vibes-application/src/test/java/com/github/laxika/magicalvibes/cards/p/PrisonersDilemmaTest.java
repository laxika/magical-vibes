package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrisonersDilemma.class})
class PrisonersDilemmaTest extends BaseCardTest {

    @Test
    void allSilenceDealsFourDamageToEachOpponent() {
        castFromHand();

        choose(player2, ChoiceContext.PrisonersDilemmaChoice.SILENCE);

        harness.assertLife(player2, 16);
        assertThat(gameLogContains("Bob chose silence")).isTrue();
    }

    @Test
    void allSnitchDealsEightDamageToEachOpponent() {
        castFromHand();

        choose(player2, ChoiceContext.PrisonersDilemmaChoice.SNITCH);

        harness.assertLife(player2, 12);
    }

    @Test
    void mixedChoicesDealTwelveDamageOnlyToSilenceChoosers() {
        Player player3 = addThirdPlayer();
        castFromHand();

        choose(player2, ChoiceContext.PrisonersDilemmaChoice.SILENCE);
        choose(player3, ChoiceContext.PrisonersDilemmaChoice.SNITCH);

        harness.assertLife(player2, 8);
        harness.assertLife(player3, 20);
    }

    @Test
    void flashbackUsesItsAlternateCostAndExilesTheSpell() {
        PrisonersDilemma dilemma = new PrisonersDilemma();
        harness.setGraveyard(player1, List.of(dilemma));
        addFlashbackMana();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        choose(player2, ChoiceContext.PrisonersDilemmaChoice.SILENCE);

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dilemma);
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new PrisonersDilemma()));
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    private void choose(Player player, String choice) {
        harness.handleListChoice(player, choice);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
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
