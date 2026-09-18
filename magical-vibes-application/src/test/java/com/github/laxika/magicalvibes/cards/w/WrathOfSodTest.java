package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathOfSod.class, GrizzlyBears.class})
class WrathOfSodTest extends BaseCardTest {

    @Test
    @DisplayName("puts manabond counters on all creatures and turns them into colored lands")
    void manabondsAllCreatures() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfSod()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownBears.getCounterCount(CounterType.MANABOND)).isOne();
        assertThat(opposingBears.getCounterCount(CounterType.MANABOND)).isOne();
        assertThat(gqs.isLand(gd, ownBears)).isTrue();
        assertThat(gqs.isCreature(gd, ownBears)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ownBears, CardSubtype.BEAR)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, ownBears)).containsExactly(CardColor.GREEN);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownBears), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }

    @Test
    @DisplayName("the manabond effect ends when the counter is removed")
    void manabondIsCounterBound() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        resolveWrath();

        bears.setCounterCount(CounterType.MANABOND, 0);

        assertThat(gqs.isLand(gd, bears)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
    }

    private void resolveWrath() {
        harness.setHand(player1, List.of(new WrathOfSod()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
