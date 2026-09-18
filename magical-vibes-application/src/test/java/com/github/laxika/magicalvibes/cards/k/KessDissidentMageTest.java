package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KessDissidentMage.class, Shock.class, Divination.class, GrizzlyBears.class})
class KessDissidentMageTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an instant from the graveyard and exiles it")
    void castsInstantFromGraveyardAndExilesIt() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    @DisplayName("Casts a sorcery from the graveyard and exiles it")
    void castsSorceryFromGraveyardAndExilesIt() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
    }

    @Test
    @DisplayName("Allows only one instant or sorcery graveyard cast each turn")
    void allowsOnlyOneCastEachTurn() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase(player1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast permanent cards from the graveyard")
    void doesNotCastPermanentCards() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only allows the graveyard cast during the controller's turn")
    void onlyAllowsCastDuringControllerTurn() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
