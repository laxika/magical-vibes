package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasMine.class, UrzasPowerPlant.class, UrzasTower.class})
class UrzasMineTest extends BaseCardTest {
    @Test
    @DisplayName("Tapping alone adds one colorless mana")
    void tapAloneAddsOne() {
        harness.addToBattlefield(player1, new UrzasMine());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping with a Power-Plant and a Tower adds two colorless mana")
    void tapWithFullTronAddsTwo() {
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasPowerPlant());
        harness.addToBattlefield(player1, new UrzasTower());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping with only a Power-Plant adds one colorless mana")
    void tapWithPartialTronAddsOne() {
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasPowerPlant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping with only a Tower adds one colorless mana")
    void tapWithOnlyTowerAddsOne() {
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasTower());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping with the other Tron lands controlled by an opponent adds one colorless mana")
    void tapWithOpponentsTronAddsOne() {
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player2, new UrzasPowerPlant());
        harness.addToBattlefield(player2, new UrzasTower());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
