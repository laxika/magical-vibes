package com.github.laxika.magicalvibes.cards.k;

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

class KarplusanForestTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Karplusan Forest has correct card properties")
    void hasCorrectProperties() {
        KarplusanForest card = new KarplusanForest();

        assertThat(card.getName()).isEqualTo("Karplusan Forest");
        assertThat(card.getType()).isEqualTo(CardType.LAND);
        assertThat(card.getActivatedAbilities()).hasSize(3);

        var colorless = card.getActivatedAbilities().get(0);
        assertThat(colorless.isRequiresTap()).isTrue();
        assertThat(colorless.getManaCost()).isNull();
        assertThat(colorless.getEffects()).hasSize(1);
        assertThat(colorless.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        var red = card.getActivatedAbilities().get(1);
        assertThat(red.isRequiresTap()).isTrue();
        assertThat(red.getEffects()).hasSize(2);
        assertThat(red.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(red.getEffects().get(1)).isInstanceOf(DealDamageToControllerEffect.class);

        var green = card.getActivatedAbilities().get(2);
        assertThat(green.isRequiresTap()).isTrue();
        assertThat(green.getEffects()).hasSize(2);
        assertThat(green.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        assertThat(green.getEffects().get(1)).isInstanceOf(DealDamageToControllerEffect.class);
    }

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new KarplusanForest());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping for red mana adds {R} and deals 1 damage to controller")
    void tapForRedMana() {
        harness.addToBattlefield(player1, new KarplusanForest());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for green mana adds {G} and deals 1 damage to controller")
    void tapForGreenMana() {
        harness.addToBattlefield(player1, new KarplusanForest());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new KarplusanForest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Colored mana abilities are mana abilities and do not use the stack")
    void coloredManaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new KarplusanForest());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }
}
