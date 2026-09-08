package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({OtterballAntics.class, GrizzlyBears.class, Shock.class})
class OtterballAnticsTest extends BaseCardTest {

    @Test
    @DisplayName("A hand cast creates a 1/1 Otter without a counter")
    void handCastCreatesOtterWithoutCounter() {
        harness.setHand(player1, List.of(new OtterballAntics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent otter = otterToken();
        assertThat(otter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
    }

    @Test
    @DisplayName("A flashback cast creates an Otter with a +1/+1 counter")
    void flashbackCastCreatesOtterWithCounter() {
        harness.setGraveyard(player1, List.of(new OtterballAntics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent otter = otterToken();
        assertThat(otter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Otter's prowess triggers for a noncreature spell")
    void otterHasProwess() {
        harness.setHand(player1, List.of(new OtterballAntics(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        Permanent otter = otterToken();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);
    }

    private Permanent otterToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
