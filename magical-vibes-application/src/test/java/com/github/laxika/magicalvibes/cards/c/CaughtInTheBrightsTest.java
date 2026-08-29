package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaughtInTheBrightsTest extends BaseCardTest {

    @Test
    @DisplayName("Caught in the Brights prevents the enchanted creature from attacking")
    void preventsAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTo(creature, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Caught in the Brights prevents the enchanted creature from blocking")
    void preventsBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachTo(blocker, player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("A Vehicle you control attacking exiles the enchanted creature")
    void vehicleAttackExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        attachTo(enchanted, player1);
        Permanent vehicle = addVehicleReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vehicle)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A non-Vehicle creature attacking does not exile the enchanted creature")
    void nonVehicleAttackDoesNotExile() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        attachTo(enchanted, player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Caught in the Brights can target only a creature")
    void targetsOnlyCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CaughtInTheBrights()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachTo(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new CaughtInTheBrights());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private Permanent addVehicleReady(Player player) {
        Permanent vehicle = new Permanent(new SkySkiff());
        vehicle.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vehicle);
        return vehicle;
    }
}
