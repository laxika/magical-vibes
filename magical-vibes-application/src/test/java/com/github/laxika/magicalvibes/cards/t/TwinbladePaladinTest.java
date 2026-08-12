package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwinbladePaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when controller gains life")
    void getsCounterOnLifeGain() {
        harness.addToBattlefield(player1, new TwinbladePaladin());
        Permanent paladin = findPaladin();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has double strike at 25 life")
    void hasDoubleStrikeAt25Life() {
        gd.playerLifeTotals.put(player1.getId(), 25);
        harness.addToBattlefield(player1, new TwinbladePaladin());

        assertThat(gqs.hasKeyword(gd, findPaladin(), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike below 25 life")
    void losesDoubleStrikeBelow25Life() {
        gd.playerLifeTotals.put(player1.getId(), 25);
        harness.addToBattlefield(player1, new TwinbladePaladin());
        Permanent paladin = findPaladin();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerLifeTotals.put(player1.getId(), 24);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent findPaladin() {
        return findPermanent(player1, "Twinblade Paladin");
    }
}
