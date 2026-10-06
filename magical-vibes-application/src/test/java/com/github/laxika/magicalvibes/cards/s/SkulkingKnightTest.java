package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.t.TelekineticSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkulkingKnight.class, SuddenShock.class, TelekineticSliver.class,
        AshcoatBear.class, BenalishCavalry.class})
class SkulkingKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, knight.getId());

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertInGraveyard(player1, "Skulking Knight");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());
        Permanent telekineticSliver = addCreatureReady(player2, new TelekineticSliver());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(telekineticSliver),
                null, knight.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertInGraveyard(player1, "Skulking Knight");
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not targeted")
    void staysWhenNotTargeted() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(knight.getId()));
    }

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent knight = addCreatureReady(player1, new SkulkingKnight());
        knight.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker with flanking")
    void blockerWithFlankingIsUnaffected() {
        Permanent knight = addCreatureReady(player1, new SkulkingKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking's penalty wears off at end of turn")
    void flankingPenaltyWearsOffAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new SkulkingKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Its controller's spell also triggers sacrifice, which uses the stack")
    void friendlyTargetingTriggersSacrifice() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, knight.getId());

        harness.assertOnBattlefield(player1, "Skulking Knight");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertInGraveyard(player1, "Skulking Knight");
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Sudden Shock");
    }

    @Test
    @DisplayName("Flanking weakens each of multiple blockers independently")
    void flankingAffectsEveryBlocker() {
        Permanent knight = addCreatureReady(player1, new SkulkingKnight());
        knight.setAttacking(true);
        Permanent first = addCreatureReady(player2, new AshcoatBear());
        Permanent second = addCreatureReady(player2, new AshcoatBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(1);
        assertThat(first.getEffectiveToughness()).isEqualTo(1);
        assertThat(second.getEffectivePower()).isEqualTo(1);
        assertThat(second.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skulking Knight");
    }

    @Test
    @DisplayName("Flanking does not trigger when Skulking Knight blocks")
    void flankingDoesNotTriggerAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        attacker.setAttacking(true);
        Permanent knight = addCreatureReady(player2, new SkulkingKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Skulking Knight");
    }
}
