package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthenAlly.class, SeaGateLoremaster.class, TajuruPreserver.class, SerraAngel.class, Forest.class})
class EarthenAllyTest extends BaseCardTest {

    @Test
    void getsPowerForDistinctColorsAmongAlliesYouControl() {
        Permanent earthenAlly = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new TajuruPreserver());
        harness.addToBattlefield(player1, new SerraAngel());

        assertThat(gqs.getEffectivePower(gd, earthenAlly)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, earthenAlly)).isEqualTo(2);
    }

    @Test
    void earthbendsTargetLandWithFiveCounters() {
        harness.addToBattlefield(player1, new EarthenAlly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addMana();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void cannotEarthbendLandControlledByOpponent() {
        harness.addToBattlefield(player1, new EarthenAlly());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
