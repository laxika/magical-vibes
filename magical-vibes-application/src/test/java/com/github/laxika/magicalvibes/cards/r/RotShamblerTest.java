package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotShambler.class, GrizzlyBears.class, Shock.class})
class RotShamblerTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureYouControlDies() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOpponentCreatureDies() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsOneCounterForEachAllyCreatureThatDies() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        harness.addToBattlefield(player1, new GrizzlyBears());
        killWithShock(player2, player1, "Grizzly Bears");

        harness.addToBattlefield(player1, new GrizzlyBears());
        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForItsOwnDeath() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, shambler.getId());

        harness.assertInGraveyard(player1, "Rot Shambler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherRotShamblerCountsAsAnotherCreature() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, dying.getId());

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void pendingTriggerDoesNotPutCountersOnAnotherShamblerAfterSourceDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RotShambler());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(gd.stack).hasSize(2);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInGraveyard(player1, "Rot Shambler");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void killWithShock(Player caster, Player targetPlayer, String targetName) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, harness.getPermanentId(targetPlayer, targetName));
        harness.passBothPriorities();
    }
}
