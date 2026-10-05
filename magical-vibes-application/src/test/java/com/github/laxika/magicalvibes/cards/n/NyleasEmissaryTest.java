package com.github.laxika.magicalvibes.cards.n;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyleasEmissary.class, TravelingPhilosopher.class})
class NyleasEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Nylea's Emissary can be cast for bestow and grants +3/+3 and trample")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new NyleasEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Nylea's Emissary");
        assertThat(gqs.isCreature(gd, emissary)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Nylea's Emissary becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new NyleasEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent emissary = findPermanent(player1, "Nylea's Emissary");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(emissary);
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Normal casting needs no target and does not boost other creatures")
    void castsNormallyWithoutTarget() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new NyleasEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Nylea's Emissary");
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new NyleasEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Nylea's Emissary");
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Nylea's Emissary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without changing its controller")
    void enchantsOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new NyleasEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Nylea's Emissary");
        assertThat(emissary.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host);
        assertThat(gqs.isCreature(gd, emissary)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, host, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The normal casting cost is insufficient to pay for bestow")
    void cannotBestowForNormalCost() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new NyleasEmissary()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Nylea's Emissary");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.TRAMPLE)).isFalse();
    }
}
