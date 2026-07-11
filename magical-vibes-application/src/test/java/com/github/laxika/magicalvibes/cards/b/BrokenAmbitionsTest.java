package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrokenAmbitionsTest extends BaseCardTest {

    private GrizzlyBears prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        harness.setHand(player2, List.of(new BrokenAmbitions()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        return bears;
    }

    // Player2 (Broken Ambitions' caster) wins the clash: their revealed top card has a strictly
    // greater mana value than player1's.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    // ===== Won clash + spell countered → that spell's controller mills four =====

    @Test
    @DisplayName("Countering and winning the clash mills the countered spell's controller four cards")
    void wonClashMillsCounteredSpellController() {
        GrizzlyBears bears = prepareCounterTarget();
        stackClashWinForCaster();
        harness.addMana(player1, ManaColor.GREEN, 2); // exactly enough to cast, nothing to pay {1}

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        // Spell was countered (player1 could not pay {1}).
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Won clash → its controller (player1) milled four cards.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
    }

    // ===== Lost clash → no mill =====

    @Test
    @DisplayName("Losing the clash counters the spell but mills nothing")
    void lostClashMillsNothing() {
        GrizzlyBears bears = prepareCounterTarget();
        // Player2 loses the clash: player1 reveals the higher mana value.
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore); // no mill
    }

    // ===== Clash mill is independent of the counter (spell not countered when X is paid) =====

    @Test
    @DisplayName("Paying X keeps the spell but a won clash still mills its controller four cards")
    void paidSpellStillMilledOnWonClash() {
        GrizzlyBears bears = prepareCounterTarget();
        stackClashWinForCaster();
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast + 1 spare to pay {1}

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        // Won clash already milled player1 before the pay prompt is offered.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // pay {1}
        harness.passBothPriorities(); // resolve Grizzly Bears

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
