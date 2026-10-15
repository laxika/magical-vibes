package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractFromDarkness.class, Forest.class, GrizzlyBears.class, HolyDay.class})
class ExtractFromDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Each player mills two cards, then a creature from any graveyard is reanimated")
    void millsEachPlayerThenReanimatesCreature() {
        Card ownMilledCard = new Forest();
        Card ownSecondMilledCard = new HolyDay();
        Card opponentMilledCard = new Forest();
        Card opponentSecondMilledCard = new HolyDay();
        Card creature = new GrizzlyBears();

        harness.setLibrary(player1, List.of(ownMilledCard, ownSecondMilledCard));
        harness.setLibrary(player2, List.of(opponentMilledCard, opponentSecondMilledCard));
        harness.setGraveyard(player2, List.of(creature));
        castExtractFromDarkness();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownMilledCard.getId(), ownSecondMilledCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        creature.getId(), opponentMilledCard.getId(), opponentSecondMilledCard.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when neither graveyard contains a creature card")
    void doesNothingWithoutCreatureCard() {
        harness.setLibrary(player1, List.of(new Forest(), new HolyDay()));
        harness.setLibrary(player2, List.of(new Forest(), new HolyDay()));
        castExtractFromDarkness();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getClass)
                .containsExactlyInAnyOrder(ExtractFromDarkness.class, Forest.class, HolyDay.class);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getClass)
                .containsExactlyInAnyOrder(Forest.class, HolyDay.class);
    }

    @Test
    @DisplayName("A creature just milled from your library can be reanimated")
    void reanimatesNewlyMilledOwnCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), creature));
        harness.setLibrary(player2, List.of(new Forest(), new HolyDay()));

        castExtractFromDarkness();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(creature.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature just milled from the opponent's library enters under your control")
    void reanimatesNewlyMilledOpponentCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new HolyDay()));
        harness.setLibrary(player2, List.of(creature, new Forest()));

        castExtractFromDarkness();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Short and empty libraries do not prevent reanimation")
    void reanimatesWithInsufficientCardsToMill() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of());

        castExtractFromDarkness();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reanimation cannot be declined when a creature card is available")
    void cannotDeclineReanimation() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castExtractFromDarkness();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castExtractFromDarkness() {
        harness.castFromHand(player1, new ExtractFromDarkness(), "{3}{U}{B}");
        harness.passBothPriorities();
    }
}
