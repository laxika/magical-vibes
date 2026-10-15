package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientTomb.class, FurnaceOfRath.class})
class AncientTombTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds {C}{C} and deals 2 damage to controller")
    void tapAddsTwoColorlessAndDealsTwoDamage() {
        harness.setLife(player1, 20);
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AncientTomb());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(tomb.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage from the ability is dealt only to its controller")
    void damagesOnlyController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AncientTomb());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AncientTomb());
        tomb.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Repeated activations accumulate mana and damage")
    void repeatedActivationsAccumulate() {
        harness.setLife(player1, 20);
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AncientTomb());

        harness.activateAbility(player1, 0, 0, null, null);
        tomb.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can activate at one life because the damage is not a cost")
    void canActivateAtOneLife() {
        harness.setLife(player1, 1);
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AncientTomb());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertLife(player1, -1);
        assertThat(tomb.isTapped()).isTrue();
        assertThat(harness.getGameData().status).isEqualTo(GameStatus.FINISHED);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The other player receives the mana and damage when controlling the land")
    void otherControllerReceivesManaAndDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent tomb = harness.addToBattlefieldAndReturn(player2, new AncientTomb());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(tomb.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Furnace of Rath doubles the damage without changing mana production")
    void damageIsDoubledByFurnaceOfRath() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AncientTomb());
        harness.addToBattlefield(player2, new FurnaceOfRath());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
