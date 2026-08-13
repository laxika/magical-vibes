package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeroOfLeinaTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying X for a spell that targets Hero of Leina Tower puts X +1/+1 counters on it")
    void payingXAddsCounters() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID heroId = harness.getPermanentId(player1, "Hero of Leina Tower");
        harness.castInstant(player1, 0, heroId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        Permanent hero = findPermanent(player1, "Hero of Leina Tower");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing X=0 for Hero of Leina Tower's heroic ability does nothing")
    void choosingZeroDoesNothing() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID heroId = harness.getPermanentId(player1, "Hero of Leina Tower");
        harness.castInstant(player1, 0, heroId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent hero = findPermanent(player1, "Hero of Leina Tower");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger Hero of Leina Tower")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent hero = findPermanent(player1, "Hero of Leina Tower");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
