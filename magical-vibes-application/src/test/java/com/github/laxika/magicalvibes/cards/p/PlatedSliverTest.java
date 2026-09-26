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
        addCreatureReady(player1, new PlatedSliver());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);

        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(baseToughness);
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
