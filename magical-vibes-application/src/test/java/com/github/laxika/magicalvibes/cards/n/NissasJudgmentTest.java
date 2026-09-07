package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissasJudgment.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class, Island.class})
class NissasJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Supports two creatures before creatures with counters deal damage")
    void supportsCreaturesThenDealsDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castNissasJudgment(List.of(bear.getId(), elves.getId(), elemental.getId()));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elemental.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("May choose no targets")
    void mayChooseNoTargets() {
        castNissasJudgment(List.of());

        harness.assertInGraveyard(player1, "Nissa's Judgment");
    }

    @Test
    @DisplayName("Requires the damage target to be a creature an opponent controls")
    void requiresOpponentCreatureForDamageTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NissasJudgment()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot support a noncreature permanent")
    void cannotSupportNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new NissasJudgment()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNissasJudgment(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new NissasJudgment()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
