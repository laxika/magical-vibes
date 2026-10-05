package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LobeliaDefenderOfBagEnd.class, FountainOfYouth.class, GrizzlyBears.class, Forest.class})
class LobeliaDefenderOfBagEndTest extends BaseCardTest {

    @Test
    void entersAndExilesEachOpponentsTopCardFaceDown() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new LobeliaDefenderOfBagEnd()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lobelia = findPermanent(player1, "Lobelia, Defender of Bag End");
        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(gd.getCardsExiledByPermanent(lobelia.getId())).containsExactly(topCard);
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void artifactSacrificeModeDrainsEachOpponent() {
        Permanent lobelia = addReadyLobelia();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), 1, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    void freePlayModeCastsOneCardExiledWithLobelia() {
        Permanent lobelia = addReadyLobelia();
        harness.addToBattlefield(player1, new FountainOfYouth());
        Card exiledCard = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCard, lobelia.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), 0, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiledCard);
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void choosesModeBeforeOpponentsCanRespond() {
        Permanent lobelia = addReadyLobelia();
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), 1, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void freePlayModeCastsTheFaceDownCardFromItsEnterTrigger() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        Permanent lobelia = harness.enterBattlefieldAndReturn(player1, new LobeliaDefenderOfBagEnd());
        harness.passBothPriorities();
        lobelia.setSummoningSick(false);
        harness.addToBattlefield(player1, new FountainOfYouth());
        assertThat(gd.findExiledCard(topCard.getId()).faceDown()).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), 0, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    void originalControllerRetainsExclusiveLookPermissionAfterControlChanges() throws Exception {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        Permanent lobelia = harness.enterBattlefieldAndReturn(player1, new LobeliaDefenderOfBagEnd());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(lobelia);
        gd.playerBattlefields.get(player2.getId()).add(lobelia);
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage originalControllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage newControllerState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        var originalControllerView = originalControllerState.battlefields().stream().flatMap(List::stream)
                .filter(permanent -> permanent.id().equals(lobelia.getId())).findFirst().orElseThrow();
        var newControllerView = newControllerState.battlefields().stream().flatMap(List::stream)
                .filter(permanent -> permanent.id().equals(lobelia.getId())).findFirst().orElseThrow();

        assertThat(originalControllerView.faceDownExiledCards()).extracting(card -> card.id())
                .containsExactly(topCard.getId());
        assertThat(newControllerView.faceDownExiledCards()).isEmpty();
        assertThat(newControllerView.faceDownExiledCount()).isEqualTo(1);
    }

    @Test
    void freePlayModeAllowsALandAndUsesTheNormalLandPlayAllowance() {
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land));
        Permanent lobelia = harness.enterBattlefieldAndReturn(player1, new LobeliaDefenderOfBagEnd());
        harness.passBothPriorities();
        lobelia.setSummoningSick(false);
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), 0, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void oneActivationAllowsOnlyOneOfTheCardsExiledWithLobelia() {
        Permanent lobelia = addReadyLobelia();
        harness.addToBattlefield(player1, new FountainOfYouth());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        gd.addToExile(player2.getId(), first, lobelia.getId(), true);
        gd.addToExile(player2.getId(), second, lobelia.getId(), true);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lobelia), 0, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void emptyOpponentLibraryDoesNotExileTheControllersTopCard() {
        Card ownTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of());
        Permanent lobelia = harness.enterBattlefieldAndReturn(player1, new LobeliaDefenderOfBagEnd());

        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(lobelia.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
    }
    private Permanent addReadyLobelia() {
        Permanent lobelia = harness.addToBattlefieldAndReturn(player1, new LobeliaDefenderOfBagEnd());
        lobelia.setSummoningSick(false);
        return lobelia;
    }
}
