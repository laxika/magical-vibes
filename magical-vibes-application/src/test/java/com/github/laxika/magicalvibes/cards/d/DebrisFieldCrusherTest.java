package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DebrisFieldCrusher.class, GrizzlyBears.class})
class DebrisFieldCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("When Debris Field Crusher enters, it deals 3 damage to a target creature")
    void enteringDealsDamageToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebrisFieldCrusher()));
        addCrusherMana();

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("When Debris Field Crusher enters, it deals 3 damage to a target player")
    void enteringDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DebrisFieldCrusher()));
        addCrusherMana();

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Debris Field Crusher")
    void stationUsesTappedCreaturePower() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new DebrisFieldCrusher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(crusher), 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(crusher.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Eight charge counters make Debris Field Crusher a flying artifact creature")
    void eightChargeCountersAnimateAndGrantFlying() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new DebrisFieldCrusher());

        crusher.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, crusher)).isFalse();
        assertThat(gqs.hasKeyword(gd, crusher, Keyword.FLYING)).isFalse();

        crusher.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, crusher)).isTrue();
        assertThat(gqs.hasKeyword(gd, crusher, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Its activated ability gives it +2/+0 until end of turn")
    void activatedAbilityBoostsCrusher() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new DebrisFieldCrusher());
        crusher.setCounterCount(CounterType.CHARGE, 8);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(crusher), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(3);
    }

    private void addCrusherMana() {
        harness.addMana(player1, ManaColor.RED, 5);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
