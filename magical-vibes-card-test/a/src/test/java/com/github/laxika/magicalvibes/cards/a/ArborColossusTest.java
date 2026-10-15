package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.s.SetessanGriffin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArborColossus.class, NessianCourser.class, SetessanGriffin.class})
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
        Permanent courser = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(courser);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Monstrosity can be activated again but has no effect once monstrous")
    void monstrosityCanBeActivatedAgainWithoutAddingCountersOrRetriggering() {
        Permanent colossus = addReadyColossus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new SetessanGriffin());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(flier);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Becoming monstrous does not destroy a friendly flier")
    void cannotTargetFriendlyFlier() {
        Permanent colossus = addReadyColossus();
        Permanent flier = harness.addToBattlefieldAndReturn(player1, new SetessanGriffin());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flier);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Two pending monstrosity activations add counters and trigger destruction only once")
    void multiplePendingActivationsBecomeMonstrousOnlyOnce() {
        Permanent colossus = addReadyColossus();
        Permanent chosenFlier = harness.addToBattlefieldAndReturn(player2, new SetessanGriffin());
        Permanent otherFlier = harness.addToBattlefieldAndReturn(player2, new SetessanGriffin());
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenFlier.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherFlier);
        harness.assertInGraveyard(player2, "Setessan Griffin");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Monstrosity can be activated while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ArborColossus());
        colossus.setSummoningSick(true);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(colossus.isMonstrous()).isTrue();
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
