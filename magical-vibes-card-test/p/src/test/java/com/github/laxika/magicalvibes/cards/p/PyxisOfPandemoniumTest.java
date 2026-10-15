package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChosenByHeliod;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyxisOfPandemonium.class, GrizzlyBears.class, Shock.class,
        ChosenByHeliod.class, NessianCourser.class, PurphorosGodOfTheForge.class})
class PyxisOfPandemoniumTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability exiles the top card of each library face down")
    void tapAbilityExilesTopCardOfEachLibraryFaceDown() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .filteredOn(entry -> pyxis.getId().equals(entry.sourcePermanentId()))
                .allSatisfy(entry -> assertThat(entry.faceDown()).isTrue());
    }

    @Test
    @DisplayName("Sacrifice ability reveals the pile and returns only permanent cards")
    void sacrificeAbilityRevealsPileAndReturnsPermanentCards() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        pyxis.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyxis of Pandemonium");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards)
                .filteredOn(entry -> pyxis.getId().equals(entry.sourcePermanentId()))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.card().getName()).isEqualTo("Shock");
                    assertThat(entry.faceDown()).isFalse();
                });
    }
    @Test
    void auraWithoutLegalEnchantedObjectRemainsFaceUpInExile() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        ChosenByHeliod aura = new ChosenByHeliod();
        harness.setLibrary(player1, List.of(aura, new NessianCourser()));
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        pyxis.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).filteredOn(entry -> entry.card().getId().equals(aura.getId()))
                .singleElement().satisfies(entry -> assertThat(entry.faceDown()).isFalse());
        harness.assertNotOnBattlefield(player1, "Chosen by Heliod");
        harness.assertNotInGraveyard(player1, "Chosen by Heliod");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneouslyEnteringPurphorosSeesEarlierCardInExiledPile() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new NessianCourser(), new PurphorosGodOfTheForge()));
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        pyxis.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        pyxis.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Nessian Courser");
        harness.assertOnBattlefield(player1, "Purphoros, God of the Forge");
        harness.assertLife(player2, 18);
    }

    @Test
    void eachPlayersPermanentReturnsUnderItsOwnersControl() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new NessianCourser()));
        harness.setLibrary(player2, List.of(new NessianCourser()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        pyxis.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Pyxis of Pandemonium");
        harness.assertNotOnBattlefield(player1, "Nessian Courser");
        harness.assertNotOnBattlefield(player2, "Nessian Courser");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nessian Courser");
        harness.assertOnBattlefield(player2, "Nessian Courser");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void neitherPlayerCanInspectFaceDownExiledCards() throws Exception {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new NessianCourser()));
        harness.setLibrary(player2, List.of(new NessianCourser()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage controllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        for (GameStateMessage state : List.of(controllerState, opponentState)) {
            var view = state.battlefields().stream().flatMap(List::stream)
                    .filter(permanent -> permanent.id().equals(pyxis.getId())).findFirst().orElseThrow();
            assertThat(view.faceDownExiledCards()).isEmpty();
            assertThat(view.faceDownExiledCount()).isEqualTo(2);
        }
    }
}
