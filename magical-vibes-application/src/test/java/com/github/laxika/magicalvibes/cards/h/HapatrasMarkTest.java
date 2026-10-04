package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HapatrasMark.class, AirElemental.class, GrizzlyBears.class, Terror.class})
class HapatrasMarkTest extends BaseCardTest {

    @Test
    @DisplayName("Grants hexproof and removes all -1/-1 counters from the target")
    void grantsHexproofAndRemovesCounters() {
        // Air Elemental (4/4) carrying three -1/-1 counters → 1/1.
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        castResolve(creature);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Only the targeted creature's counters come off")
    void leavesTheUntargetedCreaturesCountersAlone() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bystander.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castResolve(target);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(bystander.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hexproof wears off at end of turn")
    void hexproofWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(creature);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        // A controlled creature exists (spell is playable), but the opponent's creature is illegal.
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HapatrasMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID opponentId = opponent.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Grants hexproof without -1/-1 counters and preserves +1/+1 counters")
    void grantsHexproofWithoutMinusCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castResolve(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Can target your own hexproof creature and remove newly added counters")
    void canTargetOwnHexproofCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castResolve(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        castResolve(creature);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Does not resolve if the target is no longer controlled by the caster")
    void doesNotResolveAfterTargetChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player1, List.of(new HapatrasMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
        harness.assertInGraveyard(player1, "Hapatra's Mark");
    }

    @Test
    @DisplayName("Protects the creature from an opponent's removal spell already on the stack")
    void protectsAgainstRemovalOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());

        castResolve(creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Terror");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new HapatrasMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
