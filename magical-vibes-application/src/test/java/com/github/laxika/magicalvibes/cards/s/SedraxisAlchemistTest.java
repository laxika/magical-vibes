package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SedraxisAlchemistTest extends BaseCardTest {

    // ===== ETB with a blue permanent controlled =====

    @Test
    @DisplayName("ETB target is chosen as the trigger goes on the stack, not at cast time")
    void etbTargetChosenAtTriggerTime() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSedraxisAlchemist();

        // Casting the creature never asks for a target (CR 601.2c) — the gate is intervening-if.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();

        harness.passBothPriorities(); // resolve creature spell

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("ETB resolves: target nonland permanent is returned to its owner's hand")
    void etbBouncesTargetToOwnersHand() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sedraxis Alchemist enters the battlefield after resolution")
    void alchemistEntersBattlefield() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castSedraxisAlchemist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sedraxis Alchemist"));
    }

    @Test
    @DisplayName("Only nonland permanents are offered as targets — a land is excluded")
    void landIsNotAValidTarget() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Forest");
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creatureId);
        assertThat(choice.validIds()).doesNotContain(landId);
    }

    // ===== ETB without a blue permanent =====

    @Test
    @DisplayName("ETB does NOT trigger without a blue permanent — no target prompt, no bounce")
    void etbDoesNotTriggerWithoutBluePermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell

        // Intervening-if failed (CR 603.4): no trigger, no target prompt.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sedraxis Alchemist"));
    }

    // ===== Gate lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if the blue permanent is gone before resolution")
    void etbFizzlesWhenGateLost() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId); // ETB trigger on stack

        // Remove the blue permanent before the ETB resolves.
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Fugitive Wizard"));

        harness.passBothPriorities(); // resolve ETB trigger — gate no longer met

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private void setupBluePermanent() {
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castSedraxisAlchemist() {
        harness.setHand(player1, List.of(new SedraxisAlchemist()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gs.playCard(gd, player1, 0, 0, null, null);
    }
}
