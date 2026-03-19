package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithToughnessEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreeOfRedemptionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Tree of Redemption has tap ability with ExchangeLifeTotalWithToughnessEffect")
    void hasCorrectAbility() {
        TreeOfRedemption card = new TreeOfRedemption();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(ExchangeLifeTotalWithToughnessEffect.class);
    }

    // ===== Exchange behavior =====

    @Test
    @DisplayName("Exchange sets life to toughness and toughness to old life total")
    void exchangeLifeAndToughness() {
        Permanent tree = addReadyTree(player1);
        // Default starting life is 20, Tree toughness is 13
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 13 (old toughness), toughness becomes 20 (old life)
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("Exchange when life is lower than toughness raises life")
    void exchangeWhenLifeLowerThanToughness() {
        Permanent tree = addReadyTree(player1);
        gd.playerLifeTotals.put(player1.getId(), 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 13, toughness becomes 5
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exchange when life equals toughness does nothing")
    void exchangeWhenLifeEqualsToughness() {
        Permanent tree = addReadyTree(player1);
        gd.playerLifeTotals.put(player1.getId(), 13);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }

    @Test
    @DisplayName("Multiple exchanges: second exchange uses updated toughness")
    void multipleExchanges() {
        Permanent tree = addReadyTree(player1);
        // Life=20, Toughness=13

        // First exchange: life->13, toughness->20
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);

        // Untap for second activation
        tree.untap();

        // Second exchange: life->20, toughness->13
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }

    @Test
    @DisplayName("Toughness override persists across turns")
    void toughnessPersistsAcrossTurns() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Toughness is now 20
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);

        // Simulate turn reset (modifiers cleared, static recomputed)
        tree.resetModifiers();

        // Permanent base toughness override should survive
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of exchanged toughness")
    void countersApplyOnTopOfExchangedToughness() {
        Permanent tree = addReadyTree(player1);
        tree.setPlusOnePlusOneCounters(2);
        // Effective toughness = 13 + 2 = 15

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 15 (effective toughness including counters)
        // New base toughness = 20 (old life), + 2 counters = 22
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(22);
    }

    // ===== Stack behavior =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void putsAbilityOnStack() {
        addReadyTree(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tree of Redemption");
    }

    // ===== Tap cost =====

    @Test
    @DisplayName("Activating ability taps Tree of Redemption")
    void activatingTapsTree() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(tree.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent tree = new Permanent(new TreeOfRedemption());
        tree.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(tree);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent tree = addReadyTree(player1);
        tree.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyTree(Player player) {
        Permanent perm = new Permanent(new TreeOfRedemption());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
