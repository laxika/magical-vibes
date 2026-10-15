package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skyscribing.class, Forest.class})
class SkyscribingTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws X cards")
    void eachPlayerDrawsXCards() {
        harness.setHand(player1, List.of(new Skyscribing()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X=0 makes each player draw no cards")
    void zeroXDrawsNoCards() {
        harness.setHand(player1, List.of(new Skyscribing()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Forecast makes each player draw a card and keeps Skyscribing in hand")
    void forecastMakesEachPlayerDrawAndKeepsSourceInHand() {
        Skyscribing card = new Skyscribing();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(card);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Forecast can be activated only during its controller's upkeep")
    void forecastRequiresUpkeep() {
        harness.setHand(player1, List.of(new Skyscribing()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastIsLimitedToOncePerTurn() {
        harness.setHand(player1, List.of(new Skyscribing()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast can be activated again during its controller's next upkeep")
    void forecastActivationLimitResetsOnNextTurn() {
        Skyscribing card = new Skyscribing();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Forecast cannot be activated during an opponent's upkeep")
    void forecastRequiresYourUpkeep() {
        Skyscribing card = new Skyscribing();
        harness.setHand(player1, List.of(card));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }
    @Test
    @DisplayName("Forecast keeps only its source revealed until upkeep ends")
    void forecastKeepsSourceRevealedDuringUpkeep() throws Exception {
        Skyscribing source = new Skyscribing();
        harness.setHand(player1, List.of(source, new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        harness.publishState();

        String message = harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        assertThat(state.opponentHand()).extracting(card -> card.id()).containsExactly(source.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.publishState();
        message = harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        assertThat(state.opponentHand()).isEmpty();
    }

    @Test
    @DisplayName("Each copy can forecast once in the same upkeep")
    void separateCopiesCanForecastInSameUpkeep() {
        Skyscribing first = new Skyscribing();
        Skyscribing second = new Skyscribing();
        harness.setHand(player1, List.of(first, second));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateHandAbility(player1, 0, null);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        harness.activateHandAbility(player1, 1, null);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(first, second);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Forecast requires blue mana even with enough generic mana")
    void forecastRequiresBlueMana() {
        Skyscribing source = new Skyscribing();
        harness.setHand(player1, List.of(source));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(1);
    }
}
