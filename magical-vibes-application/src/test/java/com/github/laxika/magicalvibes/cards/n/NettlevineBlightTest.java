package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NettlevineBlightTest extends BaseCardTest {

    /** Attaches Nettlevine Blight (controlled by {@code auraController}) to {@code host}. */
    private Permanent attachBlight(Player auraController, Permanent host) {
        Permanent blight = new Permanent(new NettlevineBlight());
        blight.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(blight);
        return blight;
    }

    private Permanent addCreature(Player owner) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player owner) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    /** Runs {@code player} through their end step so the enchanted-controller trigger resolves. */
    private void runEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // POSTCOMBAT_MAIN -> END_STEP, trigger onto stack
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Auto-attach when exactly one other destination =====

    @Test
    @DisplayName("Sacrifices the enchanted permanent and moves onto the only other creature/land")
    void autoAttachToOnlyOtherPermanent() {
        Permanent host = addCreature(player1);
        Permanent land = addLand(player1);
        Permanent blight = attachBlight(player1, host);

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals(host.getCard().getName()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blight);
        assertThat(blight.getAttachedTo()).isEqualTo(land.getId());
    }

    // ===== No legal destination — Aura dies as a state-based action =====

    @Test
    @DisplayName("With no other creature or land, the permanent is sacrificed and the Aura is put into the graveyard")
    void noDestinationSacrificesAndAuraDies() {
        Permanent host = addCreature(player1);
        Permanent blight = attachBlight(player1, host);

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host, blight);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(host.getCard().getName()))
                .anyMatch(c -> c.getName().equals(blight.getCard().getName()));
    }

    // ===== Multiple destinations — controller chooses =====

    @Test
    @DisplayName("With multiple destinations, the enchanted permanent's controller chooses where to move it")
    void multipleDestinationsPrompt() {
        Permanent host = addCreature(player1);
        Permanent other = addCreature(player1);
        Permanent land = addLand(player1);
        Permanent blight = attachBlight(player1, host);

        runEndStep(player1); // trigger resolves, prompting for a destination

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blight, other, land);
        assertThat(blight.getAttachedTo()).isEqualTo(land.getId());
    }

    // ===== Fires on the enchanted permanent's controller's end step, not the Aura controller's =====

    @Test
    @DisplayName("Triggers only on the enchanted permanent's controller's end step")
    void triggersOnEnchantedControllerEndStepOnly() {
        Permanent host = addCreature(player2);
        Permanent land = addLand(player2);
        Permanent blight = attachBlight(player1, host); // Aura controlled by player1, on player2's creature

        // player1's end step: nothing happens (player1 doesn't control the enchanted permanent).
        runEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(blight.getAttachedTo()).isEqualTo(host.getId());

        // player2's end step: player2 sacrifices their creature and moves the Aura onto their land.
        runEndStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(host);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blight); // Aura keeps its controller
        assertThat(blight.getAttachedTo()).isEqualTo(land.getId());
    }
}
