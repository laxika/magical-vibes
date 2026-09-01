package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineshaftSpider.class, Forest.class})
class MineshaftSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may prompt")
    void enteringTheBattlefieldCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability mills two cards")
    void acceptingMayMillsTwoCards() {
        setupAndCast();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Forest(), new Forest()));
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(deck).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not mill")
    void decliningMayDoesNotMill() {
        setupAndCast();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new MineshaftSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
    }
}
