package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellweaverEternal.class, Shock.class, GrizzlyBears.class})
class SpellweaverEternalTest extends BaseCardTest {

    private Permanent addSpellweaver() {
        Permanent spellweaver = harness.addToBattlefieldAndReturn(player1, new SpellweaverEternal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return spellweaver;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent spellweaver = addSpellweaver();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spellweaver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent spellweaver = addSpellweaver();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellweaver)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent spellweaver = addSpellweaver();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellweaver)).isEqualTo(1);
    }

    @Test
    @DisplayName("Afflict 2: becoming blocked makes the defending player lose 2 life")
    void blockedAfflictsDefender() {
        Permanent atk = addCreatureReady(player1, new SpellweaverEternal());
        atk.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict is not a drain: the defender loses 2, the attacking player's life is unchanged.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void multipleNoncreatureSpellsGiveSeparateBoosts() {
        Permanent spellweaver = addSpellweaver();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spellweaver)).isEqualTo(3);
        endTurn();
        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellweaver)).isEqualTo(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent spellweaver = addSpellweaver();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellweaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellweaver)).isEqualTo(1);
    }

    @Test
    void multipleBlockersTriggerAfflictOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new SpellweaverEternal());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void afflictStillResolvesAfterAttackerIsDestroyed() {
        Permanent attacker = addCreatureReady(player1, new SpellweaverEternal());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Spellweaver Eternal");
        harness.assertLife(player2, 18);
    }
}
