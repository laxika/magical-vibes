package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CurseOfVengeance.class, Shock.class})
class CurseOfVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("A spell cast by the enchanted player adds a spite counter")
    void enchantedPlayerCastingAddsSpiteCounter() {
        Permanent curse = attachCurseToPlayer2();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(curse.getCounterCount(CounterType.SPITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell cast by the non-enchanted player does not add a spite counter")
    void nonEnchantedPlayerCastingDoesNotAddSpiteCounter() {
        Permanent curse = attachCurseToPlayer2();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(curse.getCounterCount(CounterType.SPITE)).isZero();
    }

    private Permanent attachCurseToPlayer2() {
        Permanent curse = new Permanent(new CurseOfVengeance());
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
        return curse;
    }
}
