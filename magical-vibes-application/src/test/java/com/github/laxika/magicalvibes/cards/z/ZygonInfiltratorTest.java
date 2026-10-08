package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZygonInfiltrator.class, GrizzlyBears.class, AdiposeOffspring.class})
class ZygonInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("An older copy ending does not end a newer copy with an active duration")
    void olderCopyEndingPreservesNewerCopy() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new ZygonInfiltrator());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new ZygonInfiltrator());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new AdiposeOffspring());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, firstTarget.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, secondTarget.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        harness.performUntapStep(player2);

        assertThat(firstTarget.isTapped()).isFalse();
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(infiltrator.getCard().getName()).isEqualTo("Adipose Offspring");
    }

    @Test
    @DisplayName("An already tapped target is still stunned and copied without copying its counters")
    void copiesAlreadyTappedTargetAndRetainsOwnCounters() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new ZygonInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AdiposeOffspring());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        infiltrator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(infiltrator.getCard().getName()).isEqualTo("Adipose Offspring");
        assertThat(infiltrator.getCard().getPower()).isEqualTo(2);
        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(infiltrator.getCounterCount(CounterType.STUN)).isZero();
        assertThat(infiltrator.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Taps another creature, stuns it, and becomes its copy")
    void tapsStunsAndCopiesTarget() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new ZygonInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(infiltrator.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(infiltrator.getCard().getPower()).isEqualTo(2);
        assertThat(infiltrator.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copy persists while the target remains tapped and ends when it untaps")
    void copyEndsWhenTargetUntaps() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new ZygonInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(infiltrator.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
        assertThat(infiltrator.getCard().getName()).isEqualTo("Zygon Infiltrator");
    }

    @Test
    @DisplayName("The ability cannot target the source creature")
    void cannotTargetSelf() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new ZygonInfiltrator());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, infiltrator.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability is sorcery speed")
    void sorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new ZygonInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
