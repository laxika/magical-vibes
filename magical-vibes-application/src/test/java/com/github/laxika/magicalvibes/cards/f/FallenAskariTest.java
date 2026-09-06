package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinCrusader;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallenAskari.class, DarajaGriffin.class, ZhalfirinCrusader.class})
class FallenAskariTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingHitsNonFlankingBlocker() {
        Permanent askari = addCreatureReady(player1, new FallenAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarajaGriffin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that has flanking")
    void flankingLeavesFlankingBlockerUntouched() {
        Permanent askari = addCreatureReady(player1, new FallenAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinCrusader());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking's -1/-1 effect wears off at end of turn")
    void flankingWearsOffAtEndOfTurn() {
        Permanent askari = addCreatureReady(player1, new FallenAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DarajaGriffin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fallen Askari cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new FallenAskari());
        Permanent attacker = addCreatureReady(player1, new DarajaGriffin());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
