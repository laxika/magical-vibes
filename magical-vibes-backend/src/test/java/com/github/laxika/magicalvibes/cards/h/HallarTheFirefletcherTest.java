package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallarTheFirefletcherTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    // ===== Kicked spell triggers =====

    @Test
    @DisplayName("Casting a kicked spell puts a +1/+1 counter on Hallar and deals 1 damage to each opponent")
    void kickedSpellTriggersCounterAndDamage() {
        Permanent hallar = addReadyHallar(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Academy Drake with kicker: {2}{U} + kicker {4} = 7 mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);

        // Resolve Hallar's triggered ability (counter + damage)
        harness.passBothPriorities();

        assertThat(hallar.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 1);
    }

    @Test
    @DisplayName("Second kicked spell deals 2 damage (cumulative counters)")
    void secondKickedSpellDealsCumulativeDamage() {
        Permanent hallar = addReadyHallar(player1);
        // Pre-load 1 counter from a prior kicked spell
        hallar.setPlusOnePlusOneCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        // Had 1 counter, got another = 2 counters, deals 2 damage
        assertThat(hallar.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 2);
    }

    // ===== Non-kicked spells do not trigger =====

    @Test
    @DisplayName("Casting a non-kicked spell does not trigger Hallar")
    void nonKickedSpellDoesNotTrigger() {
        Permanent hallar = addReadyHallar(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Academy Drake without kicker: {2}{U} = 3 mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hallar.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE);
    }

    @Test
    @DisplayName("Casting a spell without kicker ability does not trigger Hallar")
    void nonKickerSpellDoesNotTrigger() {
        Permanent hallar = addReadyHallar(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hallar.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE);
    }

    // ===== Opponent's kicked spells do not trigger =====

    @Test
    @DisplayName("Opponent casting a kicked spell does not trigger Hallar")
    void opponentKickedSpellDoesNotTrigger() {
        Permanent hallar = addReadyHallar(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.setHand(player2, List.of(new AcademyDrake()));

        harness.castKickedCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(hallar.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE);
    }

    // ===== Helpers =====

    private Permanent addReadyHallar(Player player) {
        Permanent perm = new Permanent(new HallarTheFirefletcher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
