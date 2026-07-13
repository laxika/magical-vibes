package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Note: the engine models a land's color as its color identity (MtgjsonOracleLoader), so
// Leechridden Swamp itself counts as one black permanent toward its "two or more black
// permanents" activation restriction. Tests are written against that engine color model.
class LeechriddenSwampTest extends BaseCardTest {

    @Test
    @DisplayName("Drain ability makes each opponent lose 1 life when controlling two or more black permanents")
    void drainWithTwoBlackPermanents() {
        Permanent swamp = addSwamp(player1);
        addBlackPermanents(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        harness.activateAbility(player1, swampIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Drain ability cannot be activated with fewer than two black permanents")
    void drainRejectedWithTooFewBlackPermanents() {
        // Only the swamp itself (one black permanent) — below the two-permanent threshold.
        Permanent swamp = addSwamp(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        assertThatThrownBy(() -> harness.activateAbility(player1, swampIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black permanents do not count toward the activation restriction")
    void nonBlackPermanentsDoNotCount() {
        Permanent swamp = addSwamp(player1);
        Permanent bears1 = new Permanent(new GrizzlyBears());
        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);
        gd.playerBattlefields.get(player1.getId()).add(bears2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Swamp counts (1), the two green creatures do not — still below the threshold.
        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        assertThatThrownBy(() -> harness.activateAbility(player1, swampIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds black mana")
    void manaAbilityAddsBlack() {
        Permanent swamp = addSwamp(player1);

        int swampIdx = gd.playerBattlefields.get(player1.getId()).indexOf(swamp);
        harness.activateAbility(player1, swampIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addSwamp(Player player) {
        harness.addToBattlefield(player, new LeechriddenSwamp());
        Permanent swamp = findPermanent(player, "Leechridden Swamp");
        swamp.setSummoningSick(false);
        swamp.untap();
        return swamp;
    }

    private void addBlackPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent p = new Permanent(i % 2 == 0 ? new WalkingCorpse() : new ZombieGoliath());
            p.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(p);
        }
    }
}
