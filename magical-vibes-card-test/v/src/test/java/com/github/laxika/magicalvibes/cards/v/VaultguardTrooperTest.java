package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VaultguardTrooper.class, GrizzlyBears.class})
class VaultguardTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("May discard its hand and draw two cards with two tapped creatures")
    void discardsHandAndDrawsTwoCardsWhenAccepted() {
        addVaultguardWithTappedCreatures(2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining does not discard or draw")
    void decliningDoesNothing() {
        addVaultguardWithTappedCreatures(2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger with fewer than two tapped creatures")
    void doesNotTriggerWithFewerThanTwoTappedCreatures() {
        addVaultguardWithTappedCreatures(1);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The tapped-creature condition is checked again when the ability resolves")
    void doesNotResolveIfARequiredTappedCreatureBecomesUntapped() {
        addVaultguardWithTappedCreatures(2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        findTappedCreature().untap();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty hand still draws two cards")
    void drawsTwoCardsWhenDiscardingAnEmptyHand() {
        addVaultguardWithTappedCreatures(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's tapped creatures do not satisfy the condition")
    void doesNotCountOpponentsTappedCreatures() {
        addVaultguardWithTappedCreatures(1);
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).tap();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Untapped creatures do not satisfy the condition")
    void doesNotCountUntappedCreatures() {
        addVaultguardWithTappedCreatures(1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Vaultguard Trooper itself counts if tapped")
    void countsItselfAsATappedCreature() {
        harness.addToBattlefieldAndReturn(player1, new VaultguardTrooper()).tap();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        addVaultguardWithTappedCreatures(2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addVaultguardWithTappedCreatures(int count) {
        harness.addToBattlefield(player1, new VaultguardTrooper());
        for (int i = 0; i < count; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            creature.tap();
        }
    }

    private Permanent findTappedCreature() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTapped())
                .findFirst()
                .orElseThrow();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
