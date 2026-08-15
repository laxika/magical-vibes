package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Hydra Broodmaster cannot activate monstrosity after becoming monstrous")
    void monstrosityOnlyResolvesOnce() {
        addReadyHydraBroodmaster();
        addMonstrosityMana(1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();
        addMonstrosityMana(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
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
