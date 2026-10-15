package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightfieldMustang.class})
class BrightfieldMustangTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle taps another creature and enables the attack trigger")
    void saddleEnablesAttackTrigger() {
        Permanent mustang = addCreatureReady(player1, new BrightfieldMustang());
        Permanent saddler = harness.addToBattlefieldAndReturn(player1, new BrightfieldMustang());
        saddler.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(mustang.isTapped()).isFalse();
        assertThat(mustang.isSaddled()).isTrue();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(mustang.isTapped()).isFalse();
        assertThat(mustang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(saddler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A departed source does not untap or put counters on another Mustang")
    void departedSourceDoesNotAffectAnotherMustang() {
        Permanent mustang = addCreatureReady(player1, new BrightfieldMustang());
        mustang.setSaddled(true);
        Permanent other = addCreatureReady(player1, new BrightfieldMustang());
        other.tap();

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(mustang);
        gd.playerGraveyards.get(player1.getId()).add(mustang.getCard());
        resolveAllTriggers();

        assertThat(other.isTapped()).isTrue();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking while saddled untaps it and puts a +1/+1 counter on it")
    void attacksWhileSaddled() {
        Permanent mustang = addCreatureReady(player1, new BrightfieldMustang());
        mustang.setSaddled(true);

        declareAttackers(player1, List.of(0));
        assertThat(mustang.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(mustang.isTapped()).isFalse();
        assertThat(mustang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, mustang)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mustang)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger")
    void doesNotTriggerWhenNotSaddled() {
        Permanent mustang = addCreatureReady(player1, new BrightfieldMustang());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(mustang.isTapped()).isTrue();
        assertThat(mustang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, mustang)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mustang)).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent mustang = addCreatureReady(player1, new BrightfieldMustang());

        declareAttackers(player1, List.of(0));
        mustang.setSaddled(true);
        resolveAllTriggers();

        assertThat(mustang.isTapped()).isTrue();
        assertThat(mustang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, mustang)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mustang)).isEqualTo(3);
    }
}
