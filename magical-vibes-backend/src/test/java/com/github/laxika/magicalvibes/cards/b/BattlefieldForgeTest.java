package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattlefieldForgeTest extends BaseCardTest {


    @Test
    @DisplayName("Battlefield Forge has correct card properties")
    void hasCorrectProperties() {
        BattlefieldForge card = new BattlefieldForge();

        assertThat(card.getActivatedAbilities()).hasSize(3);

        var colorless = card.getActivatedAbilities().get(0);
        assertThat(colorless.isRequiresTap()).isTrue();
        assertThat(colorless.getManaCost()).isNull();
        assertThat(colorless.getEffects()).hasSize(1);
        assertThat(colorless.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        var red = card.getActivatedAbilities().get(1);
        assertThat(red.isRequiresTap()).isTrue();
        assertThat(red.getManaCost()).isNull();
        assertThat(red.getEffects()).hasSize(2);
        assertThat(red.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(red.getEffects().get(1)).isInstanceOf(DealDamageToControllerEffect.class);

        var white = card.getActivatedAbilities().get(2);
        assertThat(white.isRequiresTap()).isTrue();
        assertThat(white.getManaCost()).isNull();
        assertThat(white.getEffects()).hasSize(2);
        assertThat(white.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(white.getEffects().get(1)).isInstanceOf(DealDamageToControllerEffect.class);
    }

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(forge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping for red mana adds {R} and deals 1 damage to controller")
    void tapForRedMana() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(forge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for white mana adds {W} and deals 1 damage to controller")
    void tapForWhiteMana() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(forge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new BattlefieldForge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Colored mana abilities are mana abilities and do not use the stack")
    void coloredManaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }
}
