package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemporalMastery.class, Island.class, Cancel.class})
class TemporalMasteryTest extends BaseCardTest {

    private void castNormally() {
        harness.setHand(player1, List.of(new TemporalMastery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Resolving queues an extra turn and exiles the spell")
    void resolvingQueuesExtraTurnAndExiles() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, this::castNormally);

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Temporal Mastery"));
        harness.assertNotInGraveyard(player1, "Temporal Mastery");
    }

    @Test
    @DisplayName("Drawing as the first card this turn offers a miracle reveal")
    void firstDrawOffersMiracleReveal() {
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(mastery.getId()));
    }

    @Test
    @DisplayName("A later draw this turn does not offer miracle")
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new TemporalMastery()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting miracle reveal then cast for {1}{U} resolves and grants an extra turn")
    void miracleCastGrantsExtraTurnAndExiles() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            TemporalMastery mastery = new TemporalMastery();
            harness.setLibrary(player1, List.of(mastery));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true); // reveal

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getDescription()).contains("miracle");

            harness.passBothPriorities(); // resolve miracle trigger → cast prompt
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

            harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
            harness.passBothPriorities(); // resolve Temporal Mastery

            assertThat(gd.extraTurns).containsExactly(player1.getId());
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Temporal Mastery"));
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Declining miracle reveal leaves the card in hand with no trigger")
    void decliningRevealLeavesInHand() {
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(mastery.getId()));
    }

    @Test
    @DisplayName("Declining miracle cast leaves the card in hand")
    void decliningCastLeavesInHand() {
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.passBothPriorities(); // cast prompt
        harness.handleMayAbilityChosen(player1, false); // decline cast

        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(mastery.getId()));
    }

    @Test
    @DisplayName("Miracle cast ignores sorcery timing (works off the draw)")
    void miracleCastIgnoresSorceryTiming() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () ->
                harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
                    // Not in a main phase — cast during trigger resolution mid-draw flow
                    harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
                    harness.setHand(player1, List.of());
                    harness.setHand(player2, List.of());
                    TemporalMastery mastery = new TemporalMastery();
                    harness.setLibrary(player1, List.of(mastery, new Island()));
                    harness.addMana(player1, ManaColor.BLUE, 1);
                    harness.addMana(player1, ManaColor.COLORLESS, 1);

                    harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
                    harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
                    harness.handleMayAbilityChosen(player1, true);
                    harness.passBothPriorities();
                    harness.handleMayAbilityChosen(player1, true);
                    harness.passBothPriorities();

                    assertThat(gd.extraTurns).containsExactly(player1.getId());
                }));
    }

    @Test
    @DisplayName("A countered Temporal Mastery goes to the graveyard without granting a turn")
    void counteredSpellDoesNotExileItselfOrGrantTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.setHand(player1, List.of(new TemporalMastery()));
            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player1, ManaColor.BLUE, 7);
            harness.addMana(player2, ManaColor.BLUE, 3);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castAndResolveInstant(player2, 0, gd.stack.getLast().getCard().getId());

            assertThat(gd.stack).isEmpty();
            assertThat(gd.extraTurns).isEmpty();
            harness.assertInGraveyard(player1, "Temporal Mastery");
            assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        });
    }

    @Test
    @DisplayName("Revealing with insufficient miracle mana leaves the card in hand")
    void insufficientMiracleManaDoesNotCast() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            TemporalMastery mastery = new TemporalMastery();
            harness.setLibrary(player1, List.of(mastery));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertInHand(player1, "Temporal Mastery");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.extraTurns).isEmpty();
            assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The extra turn occurs before the opponent's next normal turn")
    void extraTurnPrecedesOpponentsNormalTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.setHand(player2, List.of());
            harness.setLibrary(player1, List.of(new Island(), new Island()));
            harness.setLibrary(player2, List.of(new Island(), new Island()));
            castNormally();
            int originalTurn = gd.turnNumber;

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

            assertThat(gd.turnNumber).isEqualTo(originalTurn + 1);
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.extraTurns).isEmpty();

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

            assertThat(gd.turnNumber).isEqualTo(originalTurn + 2);
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        });
    }
}
