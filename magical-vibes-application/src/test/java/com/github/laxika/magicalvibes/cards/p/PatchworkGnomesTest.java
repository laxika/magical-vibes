package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PatchworkGnomes.class)
class PatchworkGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Patchwork Gnomes puts it onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PatchworkGnomes()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard())
                .isInstanceOf(PatchworkGnomes.class);
    }

    @Test
    @DisplayName("Activating the ability prompts for a card to discard")
    void activationStartsDiscardChoice() {
        addCreatureReady(player1, new PatchworkGnomes());
        harness.setHand(player1, List.of(new PatchworkGnomes()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card discards it and puts the ability on the stack")
    void choosingCardPaysCostAndStacksAbility() {
        addCreatureReady(player1, new PatchworkGnomes());
        harness.setHand(player1, List.of(new PatchworkGnomes()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst())
                .isInstanceOf(PatchworkGnomes.class);
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(PatchworkGnomes.class);
    }

    @Test
    @DisplayName("Cannot activate the ability with an empty hand")
    void cannotActivateWithEmptyHand() {
        addCreatureReady(player1, new PatchworkGnomes());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingGrantsRegenerationShield() {
        Permanent gnomes = addCreatureReady(player1, new PatchworkGnomes());
        harness.setHand(player1, List.of(new PatchworkGnomes()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gnomes.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Patchwork Gnomes from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent gnomes = addCreatureReady(player1, new PatchworkGnomes());
        gnomes.setRegenerationShield(1);
        gnomes.setBlocking(true);
        gnomes.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new PatchworkGnomes());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent survivor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(survivor).isSameAs(gnomes);
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.isBlocking()).isFalse();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without a regeneration shield Patchwork Gnomes dies in combat")
    void diesWithoutShield() {
        Permanent gnomes = addCreatureReady(player1, new PatchworkGnomes());
        gnomes.setBlocking(true);
        gnomes.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new PatchworkGnomes());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst())
                .isInstanceOf(PatchworkGnomes.class);
    }

    @Test
    @DisplayName("Regeneration shield clears at end of turn")
    void shieldClearsAtEndOfTurn() {
        Permanent gnomes = addCreatureReady(player1, new PatchworkGnomes());
        harness.setHand(player1, List.of(new PatchworkGnomes()));

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
        Permanent gnomes = addCreatureReady(player1, new PatchworkGnomes());
        harness.setHand(player1, List.of(new PatchworkGnomes()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gnomes.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while Patchwork Gnomes is tapped")
    void canActivateWhileTapped() {
        Permanent gnomes = addCreatureReady(player1, new PatchworkGnomes());
        gnomes.tap();
        harness.setHand(player1, List.of(new PatchworkGnomes()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gnomes.isTapped()).isTrue();
        assertThat(gnomes.getRegenerationShield()).isEqualTo(1);
    }
}
