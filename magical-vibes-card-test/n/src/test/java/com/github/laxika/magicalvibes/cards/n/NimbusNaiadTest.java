package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimbusNaiad.class, GrizzlyBears.class, BronzeSable.class})
class NimbusNaiadTest extends BaseCardTest {

    @Test
    @DisplayName("Nimbus Naiad can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent naiad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, naiad)).isTrue();
    }

    @Test
    @DisplayName("Nimbus Naiad can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent naiad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nimbus Naiad"));
        assertThat(gqs.isCreature(gd, naiad)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Nimbus Naiad becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent naiad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nimbus Naiad"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(naiad);
        assertThat(gqs.isCreature(gd, naiad)).isTrue();
        assertThat(naiad.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent naiad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nimbus Naiad"));
        assertThat(gqs.isCreature(gd, naiad)).isTrue();
        assertThat(naiad.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Nimbus Naiad");
    }

    @Test
    @DisplayName("Bestow can enchant an opposing creature and only boosts its host")
    void bestowsOnOpposingCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        int hostPower = gqs.getEffectivePower(gd, host);
        int hostToughness = gqs.getEffectiveToughness(gd, host);
        int otherPower = gqs.getEffectivePower(gd, other);
        int otherToughness = gqs.getEffectiveToughness(gd, other);
        harness.setHand(player1, List.of(new NimbusNaiad()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent naiad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nimbus Naiad"));
        assertThat(naiad.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, naiad)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(hostPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(hostToughness + 2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(otherToughness);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }
}
