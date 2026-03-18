package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StensiaBloodhallTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Stensia Bloodhall has two activated abilities")
    void hasCorrectProperties() {
        StensiaBloodhall card = new StensiaBloodhall();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability taps for colorless mana with no cost")
    void firstAbilityProperties() {
        StensiaBloodhall card = new StensiaBloodhall();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) ability.getEffects().get(0)).color()).isEqualTo(ManaColor.COLORLESS);
    }

    @Test
    @DisplayName("Second ability costs {3}{B}{R} and deals 2 damage to target player or planeswalker")
    void secondAbilityProperties() {
        StensiaBloodhall card = new StensiaBloodhall();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{3}{B}{R}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) ability.getEffects().get(0)).damage()).isEqualTo(2);
    }

    // ===== Tapping for colorless mana =====

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not use the stack")
    void tapForColorlessAddsMana() {
        Permanent bloodhall = addReadyBloodhall(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bloodhall.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Damage ability =====

    @Test
    @DisplayName("Damage ability puts entry on the stack targeting a player")
    void damageAbilityGoesOnStack() {
        Permanent bloodhall = addReadyBloodhall(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(bloodhall.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Damage ability deals 2 damage to target player on resolution")
    void dealsDamageToPlayer() {
        addReadyBloodhall(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage ability can target controller")
    void dealsDamageToSelf() {
        addReadyBloodhall(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent bloodhall = addReadyBloodhall(player1);
        bloodhall.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate damage ability without enough mana")
    void cannotActivateDamageAbilityWithoutMana() {
        addReadyBloodhall(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyBloodhall(Player player) {
        StensiaBloodhall card = new StensiaBloodhall();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
