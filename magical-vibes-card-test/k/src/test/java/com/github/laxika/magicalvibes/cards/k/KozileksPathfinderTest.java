package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KozileksPathfinder.class, Forest.class, GrizzlyBears.class})
class KozileksPathfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted creature can't block Kozilek's Pathfinder this turn")
    void targetedCreatureCannotBlockPathfinder() {
        Permanent pathfinder = addReadyPathfinder(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        pathfinder.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Targeted creature can block another creature")
    void targetedCreatureCanBlockAnotherCreature() {
        addReadyPathfinder(player1);
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Ability requires colorless mana and a creature target")
    void abilityRequiresColorlessManaAndCreatureTarget() {
        Permanent pathfinder = addReadyPathfinder(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pathfinder.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addReadyPathfinder(Player player) {
        return addCreatureReady(player, new KozileksPathfinder());
    }

    @Test
    @DisplayName("Restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent pathfinder = addReadyPathfinder(player1);
        Permanent blocker = addReadyPathfinder(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        pathfinder.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Targeted creature can block a different Kozilek's Pathfinder")
    void restrictionAppliesOnlyToTheSourcePermanent() {
        addReadyPathfinder(player1);
        Permanent otherPathfinder = addReadyPathfinder(player1);
        Permanent blocker = addReadyPathfinder(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        otherPathfinder.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Repeated activations prevent multiple creatures from blocking")
    void repeatedActivationsRestrictEachTarget() {
        Permanent pathfinder = addReadyPathfinder(player1);
        Permanent firstBlocker = addReadyPathfinder(player2);
        Permanent secondBlocker = addReadyPathfinder(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, firstBlocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, secondBlocker.getId());
        harness.passBothPriorities();

        pathfinder.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Tapped, summoning-sick Pathfinder can activate its ability")
    void abilityDoesNotRequireTappingOrHaste() {
        Permanent pathfinder = harness.addToBattlefieldAndReturn(player1, new KozileksPathfinder());
        pathfinder.setSummoningSick(true);
        pathfinder.tap();
        Permanent blocker = addReadyPathfinder(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        pathfinder.untap();
        pathfinder.setSummoningSick(false);
        pathfinder.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }
}
