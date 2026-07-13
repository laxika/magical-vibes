package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaintedAetherTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under the controller triggers Tainted Aether")
    void triggersWhenControllerCreatureEnters() {
        harness.addToBattlefield(player1, new TaintedAether());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Tainted Aether triggers

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tainted Aether");
    }

    @Test
    @DisplayName("Entering creature's controller sacrifices a chosen creature or land")
    void controllerChoosesCreatureOrLandToSacrifice() {
        harness.addToBattlefield(player1, new TaintedAether());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → controller prompted to choose

        // Two valid permanents (the entering creature + the Forest) → the controller must choose.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forestId));

        // The Forest was sacrificed; the entering creature stays.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("With only the entering creature as a valid permanent, it is auto-sacrificed")
    void autoSacrificesWhenSingleValidPermanent() {
        harness.addToBattlefield(player1, new TaintedAether());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → only the creature is valid

        // The enchantment isn't a creature or land, so the entering creature sacrifices itself.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature entering under an opponent makes that opponent sacrifice")
    void triggersForOpponentCreature() {
        harness.addToBattlefield(player1, new TaintedAether());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → opponent sacrifices

        // The entering creature's controller (player2), not Tainted Aether's controller, sacrifices.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tainted Aether"));
    }
}
