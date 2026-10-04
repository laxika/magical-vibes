package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydraBroodmaster.class})
class HydraBroodmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity X puts X counters on Hydra Broodmaster and creates X X/X Hydra tokens")
    void monstrosityUsesPaidXForCountersAndHydraTokens() {
        Permanent hydraBroodmaster = addReadyHydraBroodmaster();
        addMonstrosityMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydraBroodmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hydraBroodmaster.isMonstrous()).isTrue();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HYDRA);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Activating monstrosity again is legal but adds no counters or tokens")
    void monstrosityOnlyResolvesOnce() {
        Permanent hydraBroodmaster = addReadyHydraBroodmaster();
        addMonstrosityMana(1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        addMonstrosityMana(1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(hydraBroodmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("Monstrosity zero makes the creature monstrous without counters or tokens")
    void zeroXStillBecomesMonstrous() {
        Permanent hydraBroodmaster = harness.addToBattlefieldAndReturn(player1, new HydraBroodmaster());
        addMonstrosityMana(0);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydraBroodmaster.isMonstrous()).isTrue();
        assertThat(hydraBroodmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(hydraBroodmaster);
    }

    @Test
    @DisplayName("Only the first resolving monstrosity activation determines counters and tokens")
    void respondingWithDifferentXUsesFirstResolvingValue() {
        Permanent hydraBroodmaster = addReadyHydraBroodmaster();
        addMonstrosityMana(1);
        harness.activateAbility(player1, 0, 1, null);
        addMonstrosityMana(3);
        harness.activateAbility(player1, 0, 3, null);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydraBroodmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydraBroodmaster.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(3).allSatisfy(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(3);
                    assertThat(token.getEffectiveToughness()).isEqualTo(3);
                });
    }

    private Permanent addReadyHydraBroodmaster() {
        Permanent hydraBroodmaster = harness.addToBattlefieldAndReturn(player1, new HydraBroodmaster());
        hydraBroodmaster.setSummoningSick(false);
        return hydraBroodmaster;
    }

    private void addMonstrosityMana(int x) {
        harness.addMana(player1, ManaColor.COLORLESS, x * 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
