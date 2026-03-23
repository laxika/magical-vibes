package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeeprootChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on Deeproot Champion")
    void noncreatureSpellAddsCounter() {
        harness.addToBattlefield(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent champion = getChampion();
        assertThat(champion.getPlusOnePlusOneCounters()).isZero();

        harness.castInstant(player1, 0, player2.getId());

        // Triggered ability should be on the stack
        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Deeproot Champion"))
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve Deeproot Champion trigger

        assertThat(champion.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Deeproot Champion")
    void creatureSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent champion = getChampion();

        harness.castCreature(player1, 0);

        // Only the creature spell should be on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(champion.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger Deeproot Champion")
    void opponentNoncreatureSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new DeeprootChampion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent champion = getChampion();

        harness.castInstant(player2, 0, player1.getId());

        // Only the instant spell should be on the stack, no triggered ability
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(champion.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Multiple noncreature spells accumulate +1/+1 counters")
    void multipleNoncreatureSpellsAccumulateCounters() {
        harness.addToBattlefield(player1, new DeeprootChampion());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent champion = getChampion();

        // Cast first Shock
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve trigger

        assertThat(champion.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Cast second Shock
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve trigger

        assertThat(champion.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    private Permanent getChampion() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deeproot Champion"))
                .findFirst()
                .orElseThrow();
    }
}
