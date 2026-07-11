package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReleaseTheAntsTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ReleaseTheAnts()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    // Caster (player1) wins the clash: their top card (Grizzly Bears MV 2) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        stackClashLossForCaster();
        prepare();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void deals1DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        stackClashLossForCaster();
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(targetId) && p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Winning the clash returns Release the Ants to its owner's hand")
    void wonClashReturnsSpellToHand() {
        stackClashWinForCaster();
        prepare();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Release the Ants"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Release the Ants"));
    }

    @Test
    @DisplayName("Losing the clash sends Release the Ants to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        stackClashLossForCaster();
        prepare();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Release the Ants"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Release the Ants"));
    }
}
