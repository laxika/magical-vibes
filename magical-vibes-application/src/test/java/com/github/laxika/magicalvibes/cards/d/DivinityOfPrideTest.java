package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivinityOfPride.class, NipGwyllion.class})
class DivinityOfPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/4 at default 20 life")
    void noBoostAtDefaultLife() {
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Still 4/4 at 24 life")
    void noBoostAt24Life() {
        harness.setLife(player1, 24);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +4/+4 at exactly 25 life")
    void boostAtExactly25Life() {
        harness.setLife(player1, 25);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(8);
    }

    @Test
    @DisplayName("Loses boost when life drops below 25")
    void losesBoostWhenLifeDrops() {
        harness.setLife(player1, 25);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(8);

        harness.setLife(player1, 24);
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's life total doesn't affect the boost")
    void opponentLifeDoesNotCount() {
        harness.setLife(player2, 50);
        harness.addToBattlefield(player1, new DivinityOfPride());

        Permanent divinity = findDivinity();
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Lifelink crossing 25 life boosts the creature after dealing four damage")
    void lifelinkCrossesLifeThreshold() {
        harness.setLife(player1, 24);
        harness.addToBattlefield(player1, new DivinityOfPride());
        Permanent divinity = findDivinity();
        divinity.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 16);
        assertThat(gqs.getEffectivePower(gd, divinity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, divinity)).isEqualTo(8);
    }

    @Test
    @DisplayName("Boosted combat damage gains eight life through lifelink")
    void boostedCombatDamageHasLifelink() {
        harness.setLife(player1, 25);
        harness.addToBattlefield(player1, new DivinityOfPride());
        findDivinity().setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 33);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Each copy checks its own controller's life and boosts only itself")
    void opposingCopiesCheckTheirOwnControllers() {
        harness.setLife(player1, 25);
        harness.setLife(player2, 24);
        harness.addToBattlefield(player1, new DivinityOfPride());
        harness.addToBattlefield(player2, new DivinityOfPride());

        Permanent ownDivinity = findDivinity();
        Permanent opposingDivinity = findPermanent(player2, "Divinity of Pride");
        assertThat(gqs.getEffectivePower(gd, ownDivinity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ownDivinity)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, opposingDivinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingDivinity)).isEqualTo(4);

        harness.setLife(player1, 24);
        harness.setLife(player2, 25);

        assertThat(gqs.getEffectivePower(gd, ownDivinity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownDivinity)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingDivinity)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opposingDivinity)).isEqualTo(8);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void groundCreatureCannotBlock() {
        harness.addToBattlefield(player1, new DivinityOfPride());
        findDivinity().setSummoningSick(false);
        harness.addToBattlefield(player2, new NipGwyllion());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findDivinity() {
        return findPermanent(player1, "Divinity of Pride");
    }
}
