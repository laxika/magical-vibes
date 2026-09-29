package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfOpulence.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfOpulenceTest extends BaseCardTest {

    @Test
    @DisplayName("The Curse controller and an attacking opponent each create one Gold")
    void controllerAndAttackingOpponentCreateGold() {
        placeCurseOnPlayer(player1, player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).hasSize(1);
    }

    @Test
    @DisplayName("The Curse controller does not get a second Gold for attacking the enchanted player")
    void controllerDoesNotCreateTheOpponentGold() {
        placeCurseOnPlayer(player1, player2);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).isEmpty();
    }

    @Test
    @DisplayName("Attacking an enchanted player's planeswalker does not trigger the Curse")
    void attackingPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer(player1, player1);
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).isEmpty();
        assertThat(findPermanents(player2, "Gold")).isEmpty();
    }

    private void placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = harness.addToBattlefieldAndReturn(controller, new CurseOfOpulence());
        curse.setAttachedTo(enchantedPlayer.getId());
    }
}
