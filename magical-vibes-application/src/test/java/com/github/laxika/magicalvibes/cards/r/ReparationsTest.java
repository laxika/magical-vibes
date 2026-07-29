package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReparationsTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent spell targeting you triggers the may ability and drawing adds a card")
    void opponentSpellTargetingYouDraws() {
        harness.addToBattlefield(player1, new Reparations());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent spell targeting a creature you control triggers the may ability")
    void opponentSpellTargetingYourCreatureDraws() {
        harness.addToBattlefield(player1, new Reparations());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws nothing")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new Reparations());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Opponent spell targeting their own creature does not trigger")
    void opponentSpellTargetingOwnCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Reparations());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setUpOpponentTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Your own spell targeting you does not trigger")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Reparations());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
