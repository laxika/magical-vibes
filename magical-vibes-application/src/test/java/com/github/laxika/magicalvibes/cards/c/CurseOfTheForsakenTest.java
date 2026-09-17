package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({CurseOfTheForsaken.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfTheForsakenTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking the enchanted player makes its controller gain 1 life")
    void attackingCreatureControllerGainsLife() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Each creature attacking the enchanted player triggers separately")
    void eachAttackerTriggersSeparately() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Attacking the enchanted player's planeswalker does not trigger")
    void attackingPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = new Permanent(new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    private void placeCurseOnPlayer1() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfTheForsaken());
        curse.setAttachedTo(player1.getId());
    }
}
