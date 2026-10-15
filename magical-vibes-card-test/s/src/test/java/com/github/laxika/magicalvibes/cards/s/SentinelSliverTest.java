package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentinelSliver.class, BonescytheSliver.class, GrizzlyBears.class, Shock.class})
class SentinelSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Sentinel Sliver grants itself vigilance (it is a Sliver)")
    void grantsSelfVigilance() {
        Permanent sliver = addCreatureReady(player1, new SentinelSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Grants vigilance to another Sliver you control")
    void grantsVigilanceToOtherSliver() {
        addCreatureReady(player1, new SentinelSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant vigilance to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SentinelSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant vigilance to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new SentinelSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Slivers with granted vigilance do not tap when attacking")
    void sliversDoNotTapWhenAttacking() {
        Permanent sentinel = addCreatureReady(player1, new SentinelSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(otherSliver.isAttacking()).isTrue();
        assertThat(sentinel.isTapped()).isFalse();
        assertThat(otherSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Slivers lose vigilance when Sentinel Sliver leaves the battlefield")
    void losesVigilanceWhenSourceLeaves() {
        Permanent sentinel = addCreatureReady(player1, new SentinelSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sentinel.getId());

        harness.assertNotOnBattlefield(player1, "Sentinel Sliver");
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isFalse();
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(otherSliver.isAttacking()).isTrue();
        assertThat(otherSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A second Sentinel Sliver keeps granting vigilance after the first leaves")
    void remainingSourceKeepsVigilance() {
        Permanent first = addCreatureReady(player1, new SentinelSliver());
        Permanent second = addCreatureReady(player1, new SentinelSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, first.getId());

        assertThat(countPermanents(player1, "Sentinel Sliver")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.VIGILANCE)).isTrue();
    }
}
