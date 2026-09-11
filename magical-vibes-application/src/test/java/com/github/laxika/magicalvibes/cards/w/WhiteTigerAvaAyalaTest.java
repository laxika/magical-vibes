package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WhiteTigerAvaAyala.class)
class WhiteTigerAvaAyalaTest extends BaseCardTest {

    @Test
    void entryTurnPowerUpPutsCounterAndCreatesTigerGod() {
        Permanent whiteTiger = harness.enterBattlefieldAndReturn(player1, new WhiteTigerAvaAyala());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(whiteTiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "The Tiger God")).hasSize(1);
        assertThat(gqs.getMaxBlockersAllowed(gd, findPermanent(player1, "The Tiger God"))).isEqualTo(1);
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new WhiteTigerAvaAyala());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
