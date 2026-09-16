package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnifyingTheory.class, Firebolt.class, Island.class})
class UnifyingTheoryTest extends BaseCardTest {

    @Test
    @DisplayName("The spell's caster may pay {2} to draw a card")
    void casterPaysAndDraws() {
        harness.addToBattlefield(player1, new UnifyingTheory());
        prepareOpponentToCast();
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the payment does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new UnifyingTheory());
        prepareOpponentToCast();
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotInHand(player2, "Island");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The caster cannot draw when they cannot pay the optional cost")
    void insufficientManaDoesNotDraw() {
        harness.addToBattlefield(player1, new UnifyingTheory());
        prepareOpponentToCast();
        harness.setLibrary(player2, List.of(new Island()));

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInHand(player2, "Island");
    }

    @Test
    @DisplayName("The controller also gets the payment choice for their own spell")
    void controllerCastsAndDraws() {
        harness.addToBattlefield(player1, new UnifyingTheory());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Firebolt()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }

    private void prepareOpponentToCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 1);
    }
}
