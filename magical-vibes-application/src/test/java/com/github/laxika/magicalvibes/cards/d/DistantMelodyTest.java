package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.w.WarSpikeChangeling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DistantMelody.class, BallyrushBanneret.class, Bitterblossom.class, WarSpikeChangeling.class})
class DistantMelodyTest extends BaseCardTest {

    private void payAndCast(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.setHand(player, List.of(new DistantMelody()));
        harness.castAndResolveSorcery(player, 0, 0);
    }

    private void stockLibrary(Player player, int count) {
        List<DistantMelody> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new DistantMelody());
        }
        harness.setLibrary(player, deck);
    }

    @Test
    @DisplayName("Draws a card for each permanent of the chosen type you control")
    void drawsPerChosenTypeCount() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player1, new BallyrushBanneret());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing a type you control none of draws no cards")
    void chosenTypeYouControlNoneDrawsZero() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new WarSpikeChangeling());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player2, new BallyrushBanneret());
        harness.addToBattlefield(player2, new BallyrushBanneret());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts a noncreature Kindred permanent of the chosen type")
    void countsNoncreatureKindredPermanent() {
        harness.addToBattlefield(player1, new Bitterblossom());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.FAERIE.name());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An empty battlefield still allows a creature type choice and draws nothing")
    void emptyBattlefieldDrawsZero() {
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each cast makes a fresh creature type choice")
    void repeatedCastsChooseDifferentTypes() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player1, new Bitterblossom());
        harness.addToBattlefield(player1, new Bitterblossom());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.KITHKIN.name());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);

        payAndCast(player1);
        harness.handleListChoice(player1, CardSubtype.FAERIE.name());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
