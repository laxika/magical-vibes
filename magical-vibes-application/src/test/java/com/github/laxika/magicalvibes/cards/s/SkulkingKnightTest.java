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
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

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
}
