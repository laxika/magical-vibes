package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Note: the engine models a land's color as its color identity (MtgjsonOracleLoader), so
// Madblind Mountain itself counts as one red permanent toward its "two or more red
// permanents" activation restriction. Tests are written against that engine color model.
class MadblindMountainTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffle ability resolves when controlling two or more red permanents")
    void shuffleWithTwoRedPermanents() {
        Permanent mountain = addMountain(player1);
        addRedPermanents(player1, 1); // land itself (1) + Hill Giant (1) = two red permanents
        harness.addMana(player1, ManaColor.RED, 1);

        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        harness.activateAbility(player1, mountainIdx, 1, null, null);
        harness.passBothPriorities();

        // The {R} cost was paid and the land is tapped.
        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Shuffle ability cannot be activated with fewer than two red permanents")
    void shuffleRejectedWithTooFewRedPermanents() {
        // Only the mountain itself (one red permanent) — below the two-permanent threshold.
        Permanent mountain = addMountain(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        assertThatThrownBy(() -> harness.activateAbility(player1, mountainIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-red permanents do not count toward the activation restriction")
    void nonRedPermanentsDoNotCount() {
        Permanent mountain = addMountain(player1);
        Permanent bears1 = new Permanent(new GrizzlyBears());
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        gd.playerBattlefields.get(player1.getId()).add(bears2);
        harness.addMana(player1, ManaColor.RED, 1);

        // Mountain counts (1), the two green creatures do not — still below the threshold.
        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        assertThatThrownBy(() -> harness.activateAbility(player1, mountainIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds red mana")
    void manaAbilityAddsRed() {
        Permanent mountain = addMountain(player1);

        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        harness.activateAbility(player1, mountainIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addMountain(Player player) {
        harness.addToBattlefield(player, new MadblindMountain());
        Permanent mountain = findPermanent(player, "Madblind Mountain");
        mountain.setSummoningSick(false);
        mountain.untap();
        return mountain;
    }

    private void addRedPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent p = new Permanent(new HillGiant());
            p.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(p);
        }
    }
}
