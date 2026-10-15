package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AbzanSkycaptain;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SultaiEmissary;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorsReward.class, GrizzlyBears.class, GiantSpider.class,
        AbzanSkycaptain.class, SultaiEmissary.class})
class HonorsRewardTest extends BaseCardTest {

    @Test
    void gainsLifeAndBolstersTheLeastToughCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new HonorsReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainsLifeWithoutCreaturesAndDoesNotBolsterAnOpponentCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SultaiEmissary());
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        castReward();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Honor's Reward");
    }

    @Test
    void ignoresOpponentsLowerToughnessCreature() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AbzanSkycaptain());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SultaiEmissary());

        castReward();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tiedCreaturesRequireChoosingExactlyOneLeastToughCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SultaiEmissary());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SultaiEmissary());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new AbzanSkycaptain());
        int lifeBefore = gd.getLife(player1.getId());

        castReward();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(larger.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(larger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Honor's Reward");
    }

    @Test
    void usesCurrentToughnessIncludingExistingCounters() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new SultaiEmissary());
        emissary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent skycaptain = harness.addToBattlefieldAndReturn(player1, new AbzanSkycaptain());

        castReward();

        assertThat(emissary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(skycaptain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castReward() {
        harness.setHand(player1, List.of(new HonorsReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
