package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcyPrison.class, BalduvianBears.class, ZuranOrb.class, Disenchant.class})
class IcyPrisonTest extends BaseCardTest {

    private void castAndResolveIcyPrison(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IcyPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // resolve enchantment -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> exile
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB exiles target creature until source leaves")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new BalduvianBears());
        UUID bearsId = harness.getPermanentId(player2, "Balduvian Bears");
        castAndResolveIcyPrison(bearsId);

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Balduvian Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.setHand(player1, List.of(new IcyPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID orbId = harness.getPermanentId(player1, "Zuran Orb");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, orbId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== LTB return =====

    @Test
    @DisplayName("Exiled card returns when Icy Prison is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new BalduvianBears());
        UUID bearsId = harness.getPermanentId(player2, "Balduvian Bears");
        castAndResolveIcyPrison(bearsId);

        resetForFollowUpSpell();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        UUID prisonId = harness.getPermanentId(player1, "Icy Prison");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prisonId);

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Balduvian Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("If Icy Prison leaves before its entry trigger resolves, the creature remains exiled")
    void leavingBeforeEntryTriggerResolvesExilesCreaturePermanently() {
        harness.addToBattlefield(player2, new BalduvianBears());
        UUID bearsId = harness.getPermanentId(player2, "Balduvian Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IcyPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve Icy Prison; leave its entry trigger on the stack

        UUID prisonId = harness.getPermanentId(player1, "Icy Prison");
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, prisonId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Balduvian Bears"));
    }

    // ===== Upkeep: unless any player pays {3} =====

    @Test
    @DisplayName("Both players declining to pay sacrifices Icy Prison and returns the creature")
    void bothPlayersDecliningSacrificesAndReturns() {
        harness.addToBattlefield(player2, new BalduvianBears());
        castAndResolveIcyPrison(harness.getPermanentId(player2, "Balduvian Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> first player's may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Active player (player1) declines first
        harness.handleMayAbilityChosen(player1, false);
        // Opponent is then offered the pay
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player1, "Icy Prison");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Controller paying {3} keeps Icy Prison and the exile")
    void controllerPayingKeepsPrison() {
        harness.addToBattlefield(player2, new BalduvianBears());
        castAndResolveIcyPrison(harness.getPermanentId(player2, "Balduvian Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Icy Prison");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Balduvian Bears"));
    }

    @Test
    @DisplayName("Opponent paying {3} after controller declines keeps Icy Prison")
    void opponentPayingKeepsPrison() {
        harness.addToBattlefield(player2, new BalduvianBears());
        castAndResolveIcyPrison(harness.getPermanentId(player2, "Balduvian Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // controller declines
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true); // opponent pays

        harness.assertOnBattlefield(player1, "Icy Prison");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Balduvian Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns under its owner's control")
    void exiledCreatureReturnsToItsOwner() {
        harness.addToBattlefield(player2, new BalduvianBears());
        UUID bearsId = harness.getPermanentId(player2, "Balduvian Bears");
        gd.stolenCreatures.put(bearsId, player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new IcyPrison()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
    }
}
