package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GusthasScepter.class, GrizzlyBears.class, Ornithopter.class, Shatter.class, StealArtifact.class,
        WordOfSeizing.class})
class GusthasScepterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability exiles a chosen card from hand face down, tracked with the Scepter")
    void exilesChosenCardFaceDown() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Ornithopter())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        var exiled = gd.exiledCards.stream()
                .filter(e -> scepter.getId().equals(e.sourcePermanentId()))
                .toList();
        assertThat(exiled).singleElement().satisfies(e -> {
            assertThat(e.card().getName()).isEqualTo("Grizzly Bears");
            assertThat(e.faceDown()).isTrue();
            assertThat(e.ownerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("First ability resolves after another player gains control of the Scepter")
    void firstAbilityResolvesAfterControlChanges() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new WordOfSeizing())));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, scepter.getId());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(scepter.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.exiledCards)
                .anyMatch(e -> scepter.getId().equals(e.sourcePermanentId())
                        && e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("First ability still exiles a card if the Scepter leaves before resolution")
    void firstAbilityResolvesAfterScepterLeaves() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new Shatter())));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, scepter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getName().equals("Grizzly Bears")
                        && e.ownerId().equals(player1.getId())
                        && e.faceDown());
    }

    @Test
    @DisplayName("Control-loss cleanup waits for its triggered ability to resolve")
    void controlLossCleanupWaitsForTriggerResolution() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        exileTopHandCard(scepter);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new WordOfSeizing())));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, scepter.getId());

        assertThat(gd.exiledCards)
                .anyMatch(e -> scepter.getId().equals(e.sourcePermanentId())
                        && e.card().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Second ability returns a card exiled with the Scepter to its owner's hand")
    void returnsExiledCardToHand() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        exileTopHandCard(scepter);
        scepter.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Second ability lets its controller choose among multiple exiled cards")
    void returnsChosenCardWhenSeveralAreExiled() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        GrizzlyBears bears = new GrizzlyBears();
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, new ArrayList<>(List.of(bears, ornithopter)));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        scepter.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        scepter.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ornithopter);
        assertThat(gd.exiledCards)
                .filteredOn(e -> scepter.getId().equals(e.sourcePermanentId()))
                .extracting(e -> e.card())
                .containsExactly(bears);
    }

    @Test
    @DisplayName("Cards exiled with the Scepter go to their owner's graveyard when it leaves the battlefield")
    void exiledCardsGoToGraveyardWhenScepterLeaves() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        exileTopHandCard(scepter);

        harness.setHand(player2, new ArrayList<>(List.of(new Shatter())));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, scepter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Cards exiled with the Scepter go to their owner's graveyard when another player gains control of it")
    void exiledCardsGoToGraveyardOnControlChange() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        exileTopHandCard(scepter);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, new ArrayList<>(List.of(new StealArtifact())));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, scepter.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(scepter.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("The first ability does nothing with an empty hand")
    void exileWithEmptyHandDoesNothing() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, new ArrayList<>());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    @Test
    void returnWithNoLinkedCardsDoesNothing() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, List.of());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void eachScepterReturnsOnlyItsOwnLinkedCards() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.addToBattlefield(player1, new GusthasScepter());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        exileTopHandCard(first);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == card);
    }

    @Test
    void returningFaceDownCardDoesNotRevealItsIdentityToOpponent() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        exileTopHandCard(scepter);
        scepter.untap();
        harness.clearMessages();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.publishState();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    void originalExilerRetainsExclusiveLookPermissionAfterControlChanges() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        exileTopHandCard(scepter);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, scepter.getId());
        harness.publishState();

        assertThat(visibleFaceDownCards(harness.getConn1())).contains(card.getId());
        assertThat(visibleFaceDownCards(harness.getConn2())).doesNotContain(card.getId());
    }

    @Test
    void exilerCanLookAtCardExiledAfterScepterLeavesBattlefield() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, scepter.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.publishState();

        assertThat(gd.exiledCards).anyMatch(e -> e.card() == card);
        assertThat(visibleFaceDownCards(harness.getConn1())).contains(card.getId());
        assertThat(visibleFaceDownCards(harness.getConn2())).doesNotContain(card.getId());
    }

    @Test
    void newControllerCannotReturnCardsOwnedByPreviousController() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        exileTopHandCard(scepter);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, scepter.getId());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(e -> e.card() == card);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void controlLossTriggerMovesAllLinkedCardsToTheirRespectiveOwnersGraveyards() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        exileTopHandCard(scepter);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WordOfSeizing(), new Ornithopter()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, scepter.getId());

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.exiledCards).noneMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }

    private List<java.util.UUID> visibleFaceDownCards(FakeConnection connection) {
        GameStateMessage state = new JacksonConfig().objectMapper().readValue(connection
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        List<java.util.UUID> visible = new ArrayList<>();
        state.battlefields().stream().flatMap(List::stream)
                .flatMap(permanent -> permanent.faceDownExiledCards().stream())
                .forEach(card -> visible.add(card.id()));
        state.lookedAtExileCards().forEach(card -> visible.add(card.id()));
        return visible;
    }

    private void exileTopHandCard(Permanent scepter) {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.exiledCards).anyMatch(e -> scepter.getId().equals(e.sourcePermanentId()));
    }
}
