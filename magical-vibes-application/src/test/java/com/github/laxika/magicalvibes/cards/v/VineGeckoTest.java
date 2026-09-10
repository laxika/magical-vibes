package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VineGecko.class, AcademyDrake.class})
class VineGeckoTest extends BaseCardTest {

    @Test
    void firstKickedSpellEachTurnCostsOneLessAndPutsCounterOnVineGecko() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(vineGecko().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonKickedSpellDoesNotUseOrConsumeTheReduction() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vineGecko().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyTheFirstKickedSpellGetsTheReduction() {
        harness.addToBattlefield(player1, new VineGecko());
        harness.setHand(player1, List.of(new AcademyDrake(), new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vineGecko().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent vineGecko() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VineGecko)
                .findFirst()
                .orElseThrow();
    }
}
