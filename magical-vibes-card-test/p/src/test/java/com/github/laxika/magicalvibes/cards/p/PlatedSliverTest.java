package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.ShiftingSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlatedSliver.class, ShiftingSliver.class, FugitiveWizard.class})
class PlatedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Plated Sliver boosts itself")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new PlatedSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts Slivers controlled by either player")
    void boostsSliversControlledByEitherPlayer() {
        Permanent ownSliver = addCreatureReady(player1, new ShiftingSliver());
        Permanent opponentSliver = addCreatureReady(player2, new ShiftingSliver());
        int ownBaseToughness = gqs.getEffectiveToughness(gd, ownSliver);
        int opponentBaseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new PlatedSliver());

        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(ownBaseToughness + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(opponentBaseToughness + 1);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent opposingWizard = addCreatureReady(player2, new FugitiveWizard());
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);
        int opposingBaseToughness = gqs.getEffectiveToughness(gd, opposingWizard);

        addCreatureReady(player1, new PlatedSliver());

        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness);
        assertThat(gqs.getEffectiveToughness(gd, opposingWizard)).isEqualTo(opposingBaseToughness);
    }

    @Test
    @DisplayName("Bonuses from Plated Slivers controlled by different players stack")
    void bonusesFromMultipleSourcesStack() {
        Permanent first = addCreatureReady(player1, new PlatedSliver());
        Permanent second = addCreatureReady(player2, new PlatedSliver());
        Permanent other = addCreatureReady(player2, new ShiftingSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus starts when Plated Sliver resolves, not while it is on the stack")
    void bonusStartsOnResolution() {
        Permanent other = addCreatureReady(player2, new ShiftingSliver());

        harness.castFromHand(player1, new PlatedSliver(), "{W}");

        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        Permanent plated = findPermanent(player1, "Plated Sliver");
        assertThat(gqs.getEffectiveToughness(gd, plated)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts a Sliver that enters after Plated Sliver")
    void boostsSliverThatEntersAfterSource() {
        addCreatureReady(player1, new PlatedSliver());
        Permanent sliver = addCreatureReady(player2, new ShiftingSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus disappears when Plated Sliver leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent platedSliver = addCreatureReady(player1, new PlatedSliver());
        Permanent sliver = addCreatureReady(player2, new ShiftingSliver());

        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(platedSliver);

        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }
}
