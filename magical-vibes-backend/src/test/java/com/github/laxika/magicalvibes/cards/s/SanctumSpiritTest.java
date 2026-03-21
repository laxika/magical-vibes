package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SanctumSpiritTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sanctum Spirit has one activated ability with discard-historic cost granting indestructible")
    void hasCorrectActivatedAbility() {
        SanctumSpirit card = new SanctumSpirit();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0))
                .isInstanceOf(DiscardCardTypeCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1))
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grantEffect = (GrantKeywordEffect) card.getActivatedAbilities().getFirst().getEffects().get(1);
        assertThat(grantEffect.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(grantEffect.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Casting =====

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

    // ===== Discard historic card cost — activation =====

    @Test
    @DisplayName("Activating ability with an artifact in hand starts discard-cost choice")
    void activationWithArtifactStartsDiscardChoice() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        assertThat(gd.stack).isEmpty();
        // Only index 1 (Ornithopter, an artifact) should be valid
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Activating ability with a legendary creature in hand starts discard-cost choice")
    void activationWithLegendaryStartsDiscardChoice() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new MirriCatWarrior()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        // Only index 1 (Mirri, legendary) should be valid
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a historic card pays cost and puts ability on stack")
    void choosingHistoricCardPaysCostAndStacksAbility() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Ornithopter"));
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Sanctum Spirit");
    }

    @Test
    @DisplayName("Cannot activate without a historic card in hand")
    void cannotActivateWithoutHistoricCard() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a historic card");
    }

    @Test
    @DisplayName("Cannot choose a non-historic card for discard cost")
    void cannotChooseNonHistoricForDiscardCost() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        // Try choosing index 0 (GrizzlyBears, non-historic) — should re-prompt
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Indestructible grant =====

    @Test
    @DisplayName("Resolving ability grants indestructible until end of turn")
    void resolvingGrantsIndestructible() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

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
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Sanctum Spirit")
    void activatingDoesNotTap() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent spirit = addSpiritReady(player1);
        spirit.tap();
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap required)")
    void canActivateWithSummoningSickness() {
        Permanent spirit = new Permanent(new SanctumSpirit());
        gd.playerBattlefields.get(player1.getId()).add(spirit);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times discarding different historic cards")
    void canActivateMultipleTimes() {
        Permanent spirit = addSpiritReady(player1);
        harness.setHand(player1, List.of(new Ornithopter(), new MirriCatWarrior()));

        // First activation — discard Ornithopter
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // Second activation — discard Mirri (now at index 0 after first discard)
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Sanctum Spirit is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSpiritReady(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        // Remove spirit before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSpiritReady(Player player) {
        Permanent perm = new Permanent(new SanctumSpirit());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
