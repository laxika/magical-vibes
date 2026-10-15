package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObservantAlseid.class, TravelingPhilosopher.class})
class ObservantAlseidTest extends BaseCardTest {

    @Test
    @DisplayName("Observant Alseid can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new ObservantAlseid()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent alseid = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, alseid)).isTrue();
    }

    @Test
    @DisplayName("Observant Alseid can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ObservantAlseid()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Observant Alseid becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ObservantAlseid()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent alseid = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Observant Alseid"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alseid);
        assertThat(gqs.isCreature(gd, alseid)).isTrue();
        assertThat(alseid.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ObservantAlseid()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent alseid = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Observant Alseid"));
        assertThat(gqs.isCreature(gd, alseid)).isTrue();
        assertThat(alseid.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Observant Alseid");
    }

    @Test
    @DisplayName("Bestow can enchant an opposing creature and grants its bonus only to that creature")
    void bestowsOnOpposingCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        int hostPower = gqs.getEffectivePower(gd, host);
        int hostToughness = gqs.getEffectiveToughness(gd, host);
        int otherPower = gqs.getEffectivePower(gd, other);
        int otherToughness = gqs.getEffectiveToughness(gd, other);
        harness.setHand(player1, List.of(new ObservantAlseid()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent alseid = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Observant Alseid"));
        assertThat(alseid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, alseid)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(hostPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(hostToughness + 2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(otherToughness);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }
}
