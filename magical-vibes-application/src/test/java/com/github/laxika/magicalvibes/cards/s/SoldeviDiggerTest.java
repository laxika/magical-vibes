package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldeviDigger.class, ShieldSphere.class, SolGrail.class})
class SoldeviDiggerTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SoldeviDigger());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Puts the most recently added graveyard card on the bottom of the library")
    void bottomsTopGraveyardCard() {
        ShieldSphere shieldSphere = new ShieldSphere();
        SolGrail solGrail = new SolGrail();
        ShieldSphere libraryCard = new ShieldSphere();
        harness.setGraveyard(player1, List.of(shieldSphere, solGrail));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shieldSphere);
        List<Card> deck = List.copyOf(gd.playerDecks.get(player1.getId()));
        assertThat(deck).hasSize(2);
        assertThat(deck).containsExactly(libraryCard, solGrail);
    }

    @Test
    @DisplayName("Resolves with no effect when the graveyard is empty")
    void emptyGraveyardIsNoOp() {
        ShieldSphere libraryCard = new ShieldSphere();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Repeated activations bottom the graveyard from the top down")
    void repeatedActivationsBottomInOrder() {
        ShieldSphere shieldSphere = new ShieldSphere();
        SolGrail solGrail = new SolGrail();
        harness.setGraveyard(player1, List.of(shieldSphere, solGrail));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(List.copyOf(gd.playerDecks.get(player1.getId()))).containsExactly(solGrail, shieldSphere);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Uses only the activating player's graveyard and library")
    void usesControllerZones() {
        ShieldSphere opponentGraveyardCard = new ShieldSphere();
        SolGrail controllerLibraryCard = new SolGrail();
        ShieldSphere opponentLibraryCard = new ShieldSphere();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player1, List.of(controllerLibraryCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
    }
}
