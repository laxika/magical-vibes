package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedReincarnation.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class BlessedReincarnationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opposing creature and replaces it with the first creature from its controller's library")
    void exilesTargetAndReplacesItWithRevealedCreature() {
        harness.addToBattlefield(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player2.getId()).clear();
        gameData.playerDecks.get(player2.getId()).addAll(List.of(new FountainOfYouth(), new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Llanowar Elves"));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .anyMatch(remaining -> remaining.getName().equals("Fountain of Youth"));
        assertThat(gameData.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Shuffles the revealed cards back when the opponent's library has no creature")
    void shufflesRevealedCardsBackWhenNoCreatureIsFound() {
        harness.addToBattlefield(player2, new LlanowarElves());
        BlessedReincarnation card = new BlessedReincarnation();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player2.getId()).clear();
        gameData.playerDecks.get(player2.getId()).add(new FountainOfYouth());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(exiled -> exiled.getName().equals("Llanowar Elves"));
        assertThat(gameData.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Fountain of Youth");
        assertThat(gameData.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the spell's controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new BlessedReincarnation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
