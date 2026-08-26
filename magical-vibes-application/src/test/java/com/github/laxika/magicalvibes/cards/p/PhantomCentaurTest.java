package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomCentaur.class, DoomBlade.class, GrizzlyBears.class, Shock.class})
class PhantomCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new PhantomCentaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent centaur = findCentaur(player1);
        assertThat(centaur).isNotNull();
        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage is prevented and removes one +1/+1 counter")
    void damageIsPreventedAndRemovesOneCounter() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player2, new PhantomCentaur());
        centaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, centaur.getId());
        harness.passBothPriorities();

        Permanent survivingCentaur = findCentaur(player2);
        assertThat(survivingCentaur).isNotNull();
        assertThat(survivingCentaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player2, new PhantomCentaur());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, centaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private Permanent findCentaur(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Phantom Centaur"))
                .findFirst()
                .orElse(null);
    }
}
