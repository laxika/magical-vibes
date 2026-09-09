package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerculesPrinceOfPower.class})
class HerculesPrinceOfPowerTest extends BaseCardTest {

    @Test
    void powerUpAddsCounterAndTemporaryKeywords() {
        Permanent hercules = harness.enterBattlefieldAndReturn(player1, new HerculesPrinceOfPower());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hercules.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hercules.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(hercules.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(hercules.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void powerUpCostsFullAmountAfterEntryTurn() {
        Permanent hercules = addCreatureReady(player1, new HerculesPrinceOfPower());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hercules.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void temporaryKeywordsExpireWhileCounterRemains() {
        Permanent hercules = addCreatureReady(player1, new HerculesPrinceOfPower());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();
        hercules.resetModifiers();

        assertThat(hercules.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hercules.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(hercules.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(hercules.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        Permanent hercules = addCreatureReady(player1, new HerculesPrinceOfPower());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
