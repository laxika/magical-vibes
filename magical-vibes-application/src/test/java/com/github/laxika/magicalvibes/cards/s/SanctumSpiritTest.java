package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SanctumSpirit.class, LlanowarElves.class, DanithaCapashenParagon.class, JoustingLance.class, HistoryOfBenalia.class, Eviscerate.class})
class SanctumSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sanctum Spirit puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SanctumSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sanctum Spirit");
    }

    @Test
    @DisplayName("Resolving puts Sanctum Spirit onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SanctumSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sanctum Spirit");
    }

    @Test
    @DisplayName("Activating ability with an artifact in hand starts discard-cost choice")
    void activationWithArtifactStartsDiscardChoice() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new LlanowarElves(), new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        // Only index 1 (JoustingLance, an artifact) should be valid
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Activating ability with a legendary creature in hand starts discard-cost choice")
    void activationWithLegendaryStartsDiscardChoice() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new LlanowarElves(), new DanithaCapashenParagon()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        // Only index 1 (Danitha, legendary) should be valid
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a historic card pays cost and puts ability on stack")
    void choosingHistoricCardPaysCostAndStacksAbility() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new LlanowarElves(), new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Llanowar Elves");
        harness.assertInGraveyard(player1, "Jousting Lance");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Sanctum Spirit");
    }

    @Test
    @DisplayName("Cannot activate without a historic card in hand")
    void cannotActivateWithoutHistoricCard() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new LlanowarElves()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a historic card");
    }

    @Test
    @DisplayName("Cannot choose a non-historic card for discard cost")
    void cannotChooseNonHistoricForDiscardCost() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new LlanowarElves(), new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        // Try choosing index 0 (LlanowarElves, non-historic) — should re-prompt
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving ability grants indestructible until end of turn")
    void resolvingGrantsIndestructible() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible granted by ability resets at end of turn cleanup")
    void indestructibleResetsAtEndOfTurn() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Sanctum Spirit")
    void activatingDoesNotTap() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent spirit = addSpiritReady(player1);
        spirit.tap();
        harness.setHand(player1, List.of(new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap required)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SanctumSpirit());
        harness.setHand(player1, List.of(new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times discarding different historic cards")
    void canActivateMultipleTimes() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new JoustingLance(), new DanithaCapashenParagon()));

        // First activation — discard JoustingLance
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // Second activation — discard Danitha (now at index 0 after first discard)
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability has no effect if Sanctum Spirit is removed before resolution")
    void abilityHasNoEffectIfSourceRemoved() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new JoustingLance()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        // Remove spirit before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonlegendary Saga can pay the historic discard cost")
    void canDiscardSaga() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new LlanowarElves(), new HistoryOfBenalia()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.assertInGraveyard(player1, "History of Benalia");
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Unblocked combat damage gains life through lifelink")
    void combatDamageGainsLife() {
        Permanent spirit = addSpiritReady(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        spirit.setAttacking(true);
        spirit.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A different Sanctum Spirit does not receive the removed source's grant")
    void removedSourceDoesNotGrantToAnotherSpirit() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new JoustingLance()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new SanctumSpirit());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Jousting Lance");
    }

    @Test
    @DisplayName("Discarding a historic card in response prevents destruction")
    void activationInResponsePreventsDestruction() {
        Permanent spirit = addSpiritReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JoustingLance()));
        harness.setHand(player2, List.of(new Eviscerate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castSorcery(player2, 0, spirit.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.assertInGraveyard(player1, "Jousting Lance");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sanctum Spirit");
        harness.assertInGraveyard(player2, "Eviscerate");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSpiritReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SanctumSpirit());
        perm.setSummoningSick(false);
        return perm;
    }
}
