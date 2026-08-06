package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatchworkGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Patchwork Gnomes puts it onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PatchworkGnomes()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patchwork Gnomes");
    }

    @Test
    @DisplayName("Activating the ability prompts for a card to discard")
    void activationStartsDiscardChoice() {
        addGnomesReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card discards it and puts the ability on the stack")
    void choosingCardPaysCostAndStacksAbility() {
        addGnomesReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Patchwork Gnomes");
    }

    @Test
    @DisplayName("Cannot activate the ability with an empty hand")
    void cannotActivateWithEmptyHand() {
        addGnomesReady(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingGrantsRegenerationShield() {
        Permanent gnomes = addGnomesReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gnomes.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Patchwork Gnomes from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent gnomes = addGnomesReady(player1);
        gnomes.setRegenerationShield(1);
        gnomes.setBlocking(true);
        gnomes.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patchwork Gnomes");
        Permanent survivor = findPermanent(player1, "Patchwork Gnomes");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.isBlocking()).isFalse();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Without a regeneration shield Patchwork Gnomes dies in combat")
    void diesWithoutShield() {
        Permanent gnomes = addGnomesReady(player1);
        gnomes.setBlocking(true);
        gnomes.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Patchwork Gnomes");
    }

    @Test
    @DisplayName("Regeneration shield clears at end of turn")
    void shieldClearsAtEndOfTurn() {
        Permanent gnomes = addGnomesReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gnomes.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gnomes.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating the ability does not tap Patchwork Gnomes")
    void activatingDoesNotTap() {
        Permanent gnomes = addGnomesReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gnomes.isTapped()).isFalse();
    }

    private Permanent addGnomesReady(Player player) {
        Permanent perm = new Permanent(new PatchworkGnomes());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
