package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.l.Lignify;
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

@CardUsed({NettlevineBlight.class, WoodlandChangeling.class, Forest.class, Lignify.class})
class NettlevineBlightTest extends BaseCardTest {

    /** Attaches Nettlevine Blight (controlled by {@code auraController}) to {@code host}. */
    private Permanent attachBlight(Player auraController, Permanent host) {
        Permanent blight = harness.addToBattlefieldAndReturn(auraController, new NettlevineBlight());
        blight.setTimestamp(gd.nextTimestamp());
        blight.setAttachedTo(host.getId());
        return blight;
    }

    private Permanent addCreature(Player owner) {
        Permanent perm = harness.addToBattlefieldAndReturn(owner, new WoodlandChangeling());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addLand(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new Forest());
    }

    /** Enters the end step with triggered abilities waiting on the stack. */
    private void beginEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }

    private void runEndStep(Player player) {
        beginEndStep(player);
        harness.passBothPriorities();
    }

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

    @Test
    @DisplayName("The enchanted permanent is the source of the trigger and its controller controls it")
    void enchantedPermanentControlsGrantedTrigger() {
        Permanent host = addCreature(player2);
        addLand(player2);
        attachBlight(player1, host);

        beginEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(host.getId());
    }

    @Test
    @DisplayName("A later Lignify removes the granted end-step ability")
    void laterAbilityRemovalPreventsTrigger() {
        Permanent host = addCreature(player1);
        Permanent land = addLand(player1);
        Permanent blight = attachBlight(player1, host);
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(host, land, blight);
        assertThat(blight.getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    @DisplayName("Changing control after triggering prevents sacrifice but still moves Blight to the trigger controller's land")
    void controlChangeDoesNotChangeWhoResolvesInstruction() {
        Permanent host = addCreature(player1);
        Permanent originalControllersLand = addLand(player1);
        addLand(player2);
        Permanent blight = attachBlight(player1, host);
        beginEndStep(player1);

        gd.playerBattlefields.get(player1.getId()).remove(host);
        gd.playerBattlefields.get(player2.getId()).add(host);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blight, originalControllersLand);
        assertThat(blight.getAttachedTo()).isEqualTo(originalControllersLand.getId());
    }

    @Test
    @DisplayName("Blight can be cast on a land and continues sacrificing on subsequent end steps")
    void castOnLandAndTriggerAgainAfterMoving() {
        Permanent land = addLand(player1);
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new NettlevineBlight()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        Permanent blight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof NettlevineBlight).findFirst().orElseThrow();
        assertThat(blight.getAttachedTo()).isEqualTo(land.getId());

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(blight.getAttachedTo()).isEqualTo(creature.getId());
        runEndStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, blight);
        harness.assertInGraveyard(player1, "Nettlevine Blight");
    }
}
