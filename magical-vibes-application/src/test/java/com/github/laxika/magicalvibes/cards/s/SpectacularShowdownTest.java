package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacularShowdown.class, GrizzlyBears.class, Tatterkite.class})
class SpectacularShowdownTest extends BaseCardTest {

    @Test
    void targetedCastAddsCounterAndGoadsOnlyTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isTrue();
        assertThat(other.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gqs.isGoaded(gd, other)).isFalse();
    }

    @Test
    void overloadAddsCountersAndGoadsOnlyCreaturesThatReceiveThem() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creatureThatCantHaveCounters = harness.addToBattlefieldAndReturn(player2, new Tatterkite());
        harness.setHand(player1, List.of(new SpectacularShowdown()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.isGoaded(gd, ownCreature)).isTrue();
        assertThat(gqs.isGoaded(gd, opposingCreature)).isTrue();
        assertThat(creatureThatCantHaveCounters.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gqs.isGoaded(gd, creatureThatCantHaveCounters)).isFalse();
    }
}
