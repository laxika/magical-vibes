package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrizePig.class})
class PrizePigTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life puts that many ribbon counters on Prize Pig")
    void gainingLifePutsThatManyRibbonCountersOnPrizePig() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isEqualTo(2);
    }

    @Test
    @DisplayName("Three ribbon counters are removed and Prize Pig untaps")
    void threeRibbonCountersAreRemovedAndPigUntaps() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new PrizePig());
        pig.tap();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(pig.getCounterCount(CounterType.RIBBON)).isZero();
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prize Pig taps for a mana of any color")
    void tapsForAnyColor() {
        harness.addToBattlefield(player1, new PrizePig());
        Permanent pig = gd.playerBattlefields.get(player1.getId()).getFirst();
        pig.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
