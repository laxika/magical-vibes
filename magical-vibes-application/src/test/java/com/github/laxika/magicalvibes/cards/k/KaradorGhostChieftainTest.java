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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaradorGhostChieftain.class, GrizzlyBears.class, Shock.class})
class KaradorGhostChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less to cast for each creature card in its controller's graveyard")
    void costIsReducedByCreatureCardsInGraveyard() {
        harness.setHand(player1, List.of(new KaradorGhostChieftain()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareMainPhase();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Karador, Ghost Chieftain");
    }

    @Test
    @DisplayName("Cannot cast without enough mana when the graveyard has no creature cards")
    void nonCreatureCardsDoNotReduceCost() {
        harness.setHand(player1, List.of(new KaradorGhostChieftain()));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast one creature spell from the graveyard each of its controller's turns")
    void castsOnlyOneCreatureFromGraveyardEachTurn() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast a noncreature card from the graveyard")
    void cannotCastNonCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
