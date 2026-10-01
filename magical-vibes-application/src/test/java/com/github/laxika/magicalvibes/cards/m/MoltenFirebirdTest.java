package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoltenFirebird.class)
class MoltenFirebirdTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, returns to the battlefield at the next end step")
    void diesThenReturnsAtNextEndStep() {
        killFirebird(player1);

        harness.assertInGraveyard(player1, "Molten Firebird");
        harness.assertNotOnBattlefield(player1, "Molten Firebird");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molten Firebird");
        harness.assertNotInGraveyard(player1, "Molten Firebird");
    }

    @Test
    @DisplayName("Dies, controller skips their next draw step")
    void diesThenControllerSkipsNextDrawStep() {
        killFirebird(player1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Activated ability exiles it, so it never dies and never returns")
    void activatedAbilityExilesIt() {
        harness.addToBattlefield(player1, new MoltenFirebird());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Molten Firebird");
        harness.assertNotInGraveyard(player1, "Molten Firebird");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Molten Firebird"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Molten Firebird");
    }

    @Test
    @CardUsed(Disallow.class)
    @DisplayName("Countering the death ability counters both of its instructions")
    void counteringDeathAbilityCountersBothInstructions() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Disallow()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Permanent firebird = harness.addToBattlefieldAndReturn(player1, new MoltenFirebird());
        firebird.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passPriority(player1);
        harness.castInstant(player2, 0, firebird.getCard().getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Molten Firebird");
        harness.assertNotOnBattlefield(player1, "Molten Firebird");
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Molten Firebird");
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Returns to its owner's battlefield and skips its controller's draw step")
    void returnsToOwnerAndSkipsControllerDrawStep() {
        MoltenFirebird card = new MoltenFirebird();
        card.setOwnerId(player1.getId());
        Permanent firebird = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(firebird.getId(), player1.getId());
        killFirebird(firebird);

        harness.assertNotOnBattlefield(player2, "Molten Firebird");
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molten Firebird");
    }

    private void killFirebird(Player player) {
        killFirebird(harness.addToBattlefieldAndReturn(player, new MoltenFirebird()));
    }

    private void killFirebird(Permanent firebird) {
        firebird.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
