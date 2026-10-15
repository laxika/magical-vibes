package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.c.ClingingAnemones;
import com.github.laxika.magicalvibes.cards.g.GrislySpectacle;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaSwine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneAlleyBroker.class, ZhurTaaSwine.class, ClingingAnemones.class,
        GrislySpectacle.class, ActOfTreason.class})
class BaneAlleyBrokerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability draws a card, then exiles a chosen card from hand face down")
    void drawsThenExilesFaceDown() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        var exiled = gd.exiledCards.stream()
                .filter(e -> broker.getId().equals(e.sourcePermanentId()))
                .toList();
        assertThat(exiled).singleElement().satisfies(e -> {
            assertThat(e.card().getName()).isEqualTo("Zhur-Taa Swine");
            assertThat(e.faceDown()).isTrue();
            assertThat(e.ownerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Second ability returns a card exiled with the Broker to its owner's hand")
    void returnsExiledCardToHand() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.exiledCards).anyMatch(e -> broker.getId().equals(e.sourcePermanentId()));

        broker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Zhur-Taa Swine"));
        assertThat(gd.exiledCards).noneMatch(e -> broker.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Cards exiled with the Broker stay exiled when it leaves the battlefield")
    void exiledCardsStayExiledWhenBrokerLeaves() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        harness.setHand(player1, List.of(new ZhurTaaSwine()));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, broker.getId());

        assertThat(gd.exiledCards).anyMatch(e -> broker.getId().equals(e.sourcePermanentId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Zhur-Taa Swine"));
    }

    @Test
    void emptyHandExilesTheDrawnCard() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        var drawn = new ClingingAnemones();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).singleElement().satisfies(e -> {
            assertThat(e.card()).isSameAs(drawn);
            assertThat(e.sourcePermanentId()).isEqualTo(broker.getId());
            assertThat(e.faceDown()).isTrue();
        });
    }

    @Test
    void choosesOneOfMultipleExiledCards() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        var first = new ZhurTaaSwine();
        var second = new ClingingAnemones();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new ClingingAnemones(), new ZhurTaaSwine()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        broker.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        broker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.exiledCards).singleElement().satisfies(e -> assertThat(e.card()).isSameAs(first));
    }

    @Test
    void anotherBrokerCannotReturnTheExiledCard() {
        Permanent firstBroker = addCreatureReady(player1, new BaneAlleyBroker());
        addCreatureReady(player1, new BaneAlleyBroker());
        var exiled = new ZhurTaaSwine();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(exiled);
        assertThat(gd.exiledCards).singleElement().satisfies(e -> {
            assertThat(e.card()).isSameAs(exiled);
            assertThat(e.sourcePermanentId()).isEqualTo(firstBroker.getId());
        });
    }

    @Test
    void firstAbilityStillExilesAfterBrokerIsDestroyedInResponse() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        var exiled = new ZhurTaaSwine();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, broker.getId());
        harness.assertNotOnBattlefield(player1, "Bane Alley Broker");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(exiled).hasSize(1);
        assertThat(gd.exiledCards).singleElement().satisfies(e -> {
            assertThat(e.card()).isSameAs(exiled);
            assertThat(e.faceDown()).isTrue();
        });
    }

    @Test
    void returnAbilityStillResolvesAfterBrokerIsDestroyedInResponse() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        var exiled = new ZhurTaaSwine();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        broker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, broker.getId());
        harness.assertNotOnBattlefield(player1, "Bane Alley Broker");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiled);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void newControllerCanReturnCardToItsOriginalOwnersHand() {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        var exiled = new ZhurTaaSwine();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, broker.getId());
        harness.assertOnBattlefield(player2, "Bane Alley Broker");
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiled);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(exiled);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void previousControllerRetainsPermissionToLookAfterControlChanges() throws Exception {
        Permanent broker = addCreatureReady(player1, new BaneAlleyBroker());
        var exiled = new ZhurTaaSwine();
        harness.setHand(player1, List.of(exiled));
        harness.setLibrary(player1, List.of(new ClingingAnemones()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.publishState();
        assertThat(brokerView(harness.getConn1(), broker.getId()).faceDownExiledCards())
                .anyMatch(c -> c.id().equals(exiled.getId()));
        assertThat(brokerView(harness.getConn2(), broker.getId()).faceDownExiledCards()).isEmpty();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, broker.getId());
        harness.publishState();

        assertThat(brokerView(harness.getConn2(), broker.getId()).faceDownExiledCards())
                .anyMatch(c -> c.id().equals(exiled.getId()));
        assertThat(brokerView(harness.getConn1(), broker.getId()).faceDownExiledCards())
                .anyMatch(c -> c.id().equals(exiled.getId()));
    }

    private PermanentView brokerView(FakeConnection connection, UUID brokerId) throws Exception {
        String message = connection.getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        return state.battlefields().stream().flatMap(List::stream)
                .filter(p -> p.id().equals(brokerId)).findFirst().orElseThrow();
    }
}
