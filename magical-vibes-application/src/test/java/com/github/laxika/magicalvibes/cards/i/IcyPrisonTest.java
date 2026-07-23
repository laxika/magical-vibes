package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void advanceToUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIcyPrison(bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new IcyPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== LTB return =====

    @Test
    @DisplayName("Exiled card returns when Icy Prison is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveIcyPrison(bearsId);

        resetForFollowUpSpell();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID prisonId = harness.getPermanentId(player1, "Icy Prison");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, prisonId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    // ===== Upkeep: unless any player pays {3} =====

    @Test
    @DisplayName("Both players declining to pay sacrifices Icy Prison and returns the creature")
    void bothPlayersDecliningSacrificesAndReturns() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveIcyPrison(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep();
        harness.passBothPriorities(); // resolve upkeep trigger -> first player's may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Active player (player1) declines first
        harness.handleMayAbilityChosen(player1, false);
        // Opponent is then offered the pay
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Icy Prison"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Controller paying {3} keeps Icy Prison and the exile")
    void controllerPayingKeepsPrison() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveIcyPrison(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Icy Prison"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent paying {3} after controller declines keeps Icy Prison")
    void opponentPayingKeepsPrison() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveIcyPrison(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // controller declines
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true); // opponent pays

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Icy Prison"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
