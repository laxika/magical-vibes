package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinbladePaladin.class, AngelOfMercy.class})
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
        resolveAllTriggers();

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has double strike at 25 life")
    void hasDoubleStrikeAt25Life() {
        harness.setLife(player1, 25);
        harness.addToBattlefield(player1, new TwinbladePaladin());

        assertThat(gqs.hasKeyword(gd, findPaladin(), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike below 25 life")
    void losesDoubleStrikeBelow25Life() {
        harness.setLife(player1, 25);
        harness.addToBattlefield(player1, new TwinbladePaladin());
        Permanent paladin = findPaladin();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setLife(player1, 24);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Each separate life gain adds one counter regardless of the amount gained")
    void separateLifeGainsAddSeparateCounters() {
        harness.addToBattlefield(player1, new TwinbladePaladin());
        Permanent paladin = findPaladin();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 26);
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent gaining life does not add a counter or grant double strike")
    void opponentLifeGainDoesNotApply() {
        harness.setLife(player1, 24);
        harness.setLife(player2, 24);
        harness.addToBattlefield(player1, new TwinbladePaladin());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 27);
        assertThat(findPaladin().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, findPaladin(), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gaining life up to exactly 25 grants double strike")
    void gainsDoubleStrikeAtThreshold() {
        harness.setLife(player1, 22);
        harness.addToBattlefield(player1, new TwinbladePaladin());
        Permanent paladin = findPaladin();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 25);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent findPaladin() {
        return findPermanent(player1, "Twinblade Paladin");
    }
}
