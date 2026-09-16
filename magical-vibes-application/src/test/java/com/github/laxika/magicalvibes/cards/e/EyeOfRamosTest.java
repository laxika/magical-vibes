package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EyeOfRamos.class)
class EyeOfRamosTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability taps for blue mana")
    void firstAbilityAddsBlueMana() {
        harness.addToBattlefield(player1, new EyeOfRamos());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability sacrifices itself to add blue mana")
    void secondAbilitySacrificesForBlueMana() {
        harness.addToBattlefield(player1, new EyeOfRamos());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice ability can be activated while Eye of Ramos is tapped")
    void secondAbilityDoesNotRequireUntappedArtifact() {
        harness.addToBattlefield(player1, new EyeOfRamos());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
