package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturesRevolt.class, Forest.class, Mountain.class, GrizzlyBears.class, GloriousAnthem.class})
class NaturesRevoltTest extends BaseCardTest {

    @Test
    @DisplayName("Lands of both players become 2/2 creatures that are still lands")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.isLand(gd, forest)).isTrue();

        Permanent mountain = findPermanent(player2, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Lands entering after Nature's Revolt also become 2/2 creatures")
    void animatesLandsThatEnterLater() {
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.enterBattlefieldAndReturn(player2, new Mountain());

        Permanent mountain = findPermanent(player2, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Does not animate non-land permanents or change existing creatures")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        // Grizzly Bears is a natural 2/2 — Nature's Revolt does not touch its P/T.
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated lands benefit from a creature anthem")
    void animatedLandsBenefitFromAnthem() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent forest = findPermanent(player1, "Forest");
        // 2/2 from Nature's Revolt + 1/1 from Glorious Anthem = 3/3.
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands revert to non-creatures when Nature's Revolt leaves")
    void revertsWhenLeaves() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent forest = findPermanent(player1, "Forest");
        Permanent revolt = findPermanent(player1, "Nature's Revolt");
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, revolt));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }
}
