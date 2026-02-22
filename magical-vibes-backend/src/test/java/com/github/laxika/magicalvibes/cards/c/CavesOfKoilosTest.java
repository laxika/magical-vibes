package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CavesOfKoilosTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Caves of Koilos has correct card properties")
    void hasCorrectProperties() {
        CavesOfKoilos card = new CavesOfKoilos();

        assertThat(card.getName()).isEqualTo("Caves of Koilos");
        assertThat(card.getType()).isEqualTo(CardType.LAND);
        assertThat(card.getManaCost()).isNull();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("First ability taps for colorless mana with no damage")
    void firstAbilityProperties() {
        CavesOfKoilos card = new CavesOfKoilos();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) ability.getEffects().get(0)).color()).isEqualTo(ManaColor.COLORLESS);
    }

    @Test
    @DisplayName("Second ability taps for white mana and deals 1 damage")
    void secondAbilityProperties() {
        CavesOfKoilos card = new CavesOfKoilos();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) ability.getEffects().get(0)).color()).isEqualTo(ManaColor.WHITE);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DealDamageToControllerEffect.class);
        assertThat(((DealDamageToControllerEffect) ability.getEffects().get(1)).damage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Third ability taps for black mana and deals 1 damage")
    void thirdAbilityProperties() {
        CavesOfKoilos card = new CavesOfKoilos();

        var ability = card.getActivatedAbilities().get(2);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) ability.getEffects().get(0)).color()).isEqualTo(ManaColor.BLACK);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DealDamageToControllerEffect.class);
        assertThat(((DealDamageToControllerEffect) ability.getEffects().get(1)).damage()).isEqualTo(1);
    }

    // ===== Tapping for colorless mana =====

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not deal damage")
    void tapForColorlessAddsManaNoDamage() {
        harness.setLife(player1, 20);
        Permanent caves = addReadyCaves(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(caves.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Tapping for white mana =====

    @Test
    @DisplayName("Tapping for white adds {W} and deals 1 damage to controller")
    void tapForWhiteAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCaves(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Tapping for black mana =====

    @Test
    @DisplayName("Tapping for black adds {B} and deals 1 damage to controller")
    void tapForBlackAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCaves(player1);

        harness.activateAbility(player1, 0, 2, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent caves = addReadyCaves(player1);
        caves.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Cumulative damage =====

    @Test
    @DisplayName("Multiple pain land activations across turns accumulate damage")
    void cumulativeDamageAcrossTurns() {
        harness.setLife(player1, 20);
        Permanent caves = addReadyCaves(player1);

        // Tap for white — 1 damage
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);

        // Untap and tap for black — 1 more damage
        caves.untap();
        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(18);

        // Untap and tap for colorless — no damage
        caves.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Helpers =====

    private Permanent addReadyCaves(Player player) {
        CavesOfKoilos card = new CavesOfKoilos();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
