package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolfsQuarry.class, LightningBolt.class})
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

    private void castWolfsQuarry() {
        harness.setHand(player1, List.of(new WolfsQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void castAndKillBoar() {
        castWolfsQuarry();
        Permanent boar = findPermanent(player1, "Boar");

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, boar.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
