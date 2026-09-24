package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KessDissidentMage.class, Shock.class, GrizzlyBears.class})
class KessDissidentMageTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an instant from the graveyard and exiles it after resolution")
    void castsInstantFromGraveyardAndExilesIt() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    @DisplayName("Allows only one instant or sorcery graveyard cast each turn")
    void allowsOnlyOneSpellFromGraveyardEachTurn() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow permanent cards from the graveyard")
    void doesNotAllowPermanentCards() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow a graveyard cast during an opponent's turn")
    void doesNotAllowCastDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
