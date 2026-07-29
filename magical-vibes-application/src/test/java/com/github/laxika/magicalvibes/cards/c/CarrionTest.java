package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrionTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Carrion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private long insectCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Insect".equals(permanent.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("Sacrificing a 2-power creature creates two 0/1 Insects")
    void sacrificeTwoPowerCreatureCreatesTwoInsects() {
        Permanent sacrifice = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        prepare();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(insectCount()).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Insect".equals(permanent.getCard().getName()))
                .forEach(insect -> {
                    assertThat(insect.getEffectivePower()).isZero();
                    assertThat(insect.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Sacrificed creature's power counts +1/+1 counters")
    void sacrificedPowerIncludesCounters() {
        Permanent sacrifice = new Permanent(new AirElemental()); // 4/4
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // 6/6
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        prepare();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(insectCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);
        prepare();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
