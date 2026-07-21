package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalMasteryTest extends BaseCardTest {

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void castNormally() {
        harness.setHand(player1, List.of(new TemporalMastery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving queues an extra turn and exiles the spell")
    void resolvingQueuesExtraTurnAndExiles() {
        enableAutoStop();
        castNormally();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Temporal Mastery"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Temporal Mastery"));
    }

    @Test
    @DisplayName("Drawing as the first card this turn offers a miracle reveal")
    void firstDrawOffersMiracleReveal() {
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(mastery.getId()));
    }

    @Test
    @DisplayName("A later draw this turn does not offer miracle")
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new TemporalMastery()));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting miracle reveal then cast for {1}{U} resolves and grants an extra turn")
    void miracleCastGrantsExtraTurnAndExiles() {
        enableAutoStop();
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);
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
    }

    @Test
    @DisplayName("Declining miracle reveal leaves the card in hand with no trigger")
    void decliningRevealLeavesInHand() {
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery));

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);
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

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);
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
        enableAutoStop();
        // Not in a main phase — cast during trigger resolution mid-draw flow
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        TemporalMastery mastery = new TemporalMastery();
        harness.setLibrary(player1, List.of(mastery, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getPlayerInputService().processNextMayAbility(gd);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }
}
