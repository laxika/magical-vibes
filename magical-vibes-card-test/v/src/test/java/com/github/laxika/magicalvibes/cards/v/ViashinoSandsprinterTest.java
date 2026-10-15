package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoSandsprinter.class, MotherBear.class})
class ViashinoSandsprinterTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent sandsprinter = harness.addToBattlefieldAndReturn(player1, new ViashinoSandsprinter());
        sandsprinter.setSummoningSick(true);
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);

        Permanent sandsprinter = addCreatureReady(player1, new ViashinoSandsprinter());
        sandsprinter.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new MotherBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Mother Bear");
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the beginning of the end step")
    void returnsItselfToHandAtEndStep() {
        harness.addToBattlefieldAndReturn(player1, new ViashinoSandsprinter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Viashino Sandsprinter");
        harness.assertInHand(player1, "Viashino Sandsprinter");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ViashinoSandsprinter()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Viashino Sandsprinter");
        harness.assertInHand(player1, "Mother Bear");
    }

    @Test
    @DisplayName("Returns at the opponent's end step as well")
    void returnsAtOpponentsEndStep() {
        harness.addToBattlefieldAndReturn(player1, new ViashinoSandsprinter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Viashino Sandsprinter");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Viashino Sandsprinter");
        harness.assertInHand(player1, "Viashino Sandsprinter");
    }

    @Test
    @DisplayName("Entering after the end step begins does not trigger an immediate return")
    void enteringDuringEndStepWaitsForNextEndStep() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new MotherBear()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.addToBattlefieldAndReturn(player1, new ViashinoSandsprinter());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Viashino Sandsprinter");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Viashino Sandsprinter");
        harness.assertInHand(player1, "Viashino Sandsprinter");
    }

    @Test
    @DisplayName("Cycling discards as a cost before the draw resolves")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new ViashinoSandsprinter()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Viashino Sandsprinter");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Mother Bear");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with nonred mana")
    void cyclingRequiresRedMana() {
        harness.setHand(player1, List.of(new ViashinoSandsprinter()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Viashino Sandsprinter");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen Sandsprinter returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        Permanent sandsprinter = harness.addToBattlefieldAndReturn(player1, new ViashinoSandsprinter());
        gd.playerBattlefields.get(player1.getId()).remove(sandsprinter);
        gd.playerBattlefields.get(player2.getId()).add(sandsprinter);
        gd.stolenCreatures.put(sandsprinter.getId(), player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Viashino Sandsprinter");
        harness.assertInHand(player1, "Viashino Sandsprinter");
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Viashino Sandsprinter"));
    }
}
