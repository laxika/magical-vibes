package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HammerheadMaggiaBoss.class, GrizzlyBears.class, Spellbook.class})
class HammerheadMaggiaBossTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Hammerhead")
    void sacrificingAnotherCreaturePutsCounter() {
        Permanent hammerhead = addCreatureReady(player1, new HammerheadMaggiaBoss());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hammerhead.getEffectivePower()).isEqualTo(3);
        assertThat(hammerhead.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a +1/+1 counter on Hammerhead")
    void sacrificingAnotherArtifactPutsCounter() {
        Permanent hammerhead = addCreatureReady(player1, new HammerheadMaggiaBoss());
        harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hammerhead.getEffectivePower()).isEqualTo(3);
        assertThat(hammerhead.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Hammerhead itself")
    void activatedAbilityRequiresAnotherPermanent() {
        addCreatureReady(player1, new HammerheadMaggiaBoss());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately but the counter waits for resolution")
    void sacrificeIsPaidBeforeResolution() {
        Permanent hammerhead = addCreatureReady(player1, new HammerheadMaggiaBoss());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hammerhead can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hammerhead = harness.addToBattlefieldAndReturn(player1, new HammerheadMaggiaBoss());
        hammerhead.setSummoningSick(true);
        hammerhead.tap();
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Opponent's creatures and artifacts cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPermanents() {
        Permanent hammerhead = addCreatureReady(player1, new HammerheadMaggiaBoss());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each activation sacrifices one chosen permanent and adds one counter")
    void multipleActivationsEachPayTheirOwnCost() {
        Permanent hammerhead = addCreatureReady(player1, new HammerheadMaggiaBoss());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbook.getId());

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(hammerhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
