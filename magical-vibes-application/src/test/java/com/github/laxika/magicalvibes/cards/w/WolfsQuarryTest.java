package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.ScorchingDragonfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfsQuarry.class, LightningBolt.class, ScorchingDragonfire.class})
class WolfsQuarryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three Boar creature tokens")
    void createsThreeBoars() {
        castWolfsQuarry();

        assertThat(countPermanents(player1, "Boar")).isEqualTo(3);
    }

    @Test
    @DisplayName("A Boar's death creates a Food token")
    void boarDeathCreatesFood() {
        castAndKillBoar();

        assertThat(countPermanents(player1, "Boar")).isEqualTo(2);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Food created by a Boar can be sacrificed for 3 life")
    void foodCanBeSacrificedForLife() {
        castAndKillBoar();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Exiling a Boar instead of its death creates no Food")
    void exiledBoarCreatesNoFood() {
        castWolfsQuarry();
        Permanent boar = findPermanent(player1, "Boar");
        harness.setHand(player1, List.of(new ScorchingDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, boar.getId());

        assertThat(countPermanents(player1, "Boar")).isEqualTo(2);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and life is gained only on resolution")
    void foodSacrificePrecedesLifeGain() {
        castAndKillBoar();
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("A tapped Food cannot activate its life gain ability")
    void tappedFoodCannotBeActivated() {
        castAndKillBoar();
        Permanent food = findPermanent(player1, "Food");
        food.tap();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Food requires two mana to activate")
    void foodRequiresTwoMana() {
        castAndKillBoar();
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(food.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void castWolfsQuarry() {
        harness.setHand(player1, List.of(new WolfsQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void castAndKillBoar() {
        castWolfsQuarry();
        Permanent boar = findPermanent(player1, "Boar");

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, boar.getId());
        harness.passBothPriorities();
    }
}
