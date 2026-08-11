package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SetessanGriffin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArborColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Arbor Colossus and destroys a chosen opposing flier")
    void becomingMonstrousAddsCountersAndDestroysOpposingFlier() {
        Permanent colossus = addReadyColossus();
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new SetessanGriffin());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, flier.getId());
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(colossus.isMonstrous()).isTrue();
        harness.assertInGraveyard(player2, "Setessan Griffin");
    }

    @Test
    @DisplayName("Arbor Colossus cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        Permanent colossus = addReadyColossus();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Arbor Colossus cannot be activated again after becoming monstrous")
    void monstrosityOnlyResolvesOnce() {
        addReadyColossus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyColossus() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ArborColossus());
        colossus.setSummoningSick(false);
        return colossus;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);
    }
}
