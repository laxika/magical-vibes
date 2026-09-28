package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MireTriton;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KathrilAspectWarper.class, GrizzlyBears.class, MireTriton.class, SerraAngel.class})
class KathrilAspectWarperTest extends BaseCardTest {

    @Test
    void distributesOneCounterForEachKeywordFoundAndBoostsKathril() {
        harness.setGraveyard(player1, List.of(new SerraAngel(), new MireTriton()));
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        chooseCreatureForCounter(otherCreature, CounterType.FLYING);
        chooseCreatureForCounter(otherCreature, CounterType.DEATHTOUCH);
        chooseCreatureForCounter(otherCreature, CounterType.VIGILANCE);

        assertThat(otherCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void ignoresUnlistedKeywordsAndOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new SerraAngel()));

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(kathril.getTotalCounterCount()).isZero();
    }

    private void chooseCreatureForCounter(Permanent creature, CounterType counterType) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(creature.getCounterCount(counterType)).isEqualTo(1);
    }
}
