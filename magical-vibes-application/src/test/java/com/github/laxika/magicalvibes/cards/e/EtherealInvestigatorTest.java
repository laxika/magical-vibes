package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
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

@CardUsed({EtherealInvestigator.class, GrizzlyBears.class})
class EtherealInvestigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one Clue for each opponent")
    void investigatesForEachOpponent() {
        addThirdPlayer();

        harness.enterBattlefieldAndReturn(player1, new EtherealInvestigator());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a flying Spirit when you draw your second card each turn")
    void secondDrawCreatesSpirit() {
        harness.addToBattlefield(player1, new EtherealInvestigator());
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
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
