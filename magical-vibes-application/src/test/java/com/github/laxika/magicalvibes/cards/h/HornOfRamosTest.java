package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornOfRamos.class})
class HornOfRamosTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one green mana")
    void tapAddsOneGreenMana() {
        harness.addToBattlefield(player1, new HornOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Horn of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability adds one green mana and moves the artifact to the graveyard")
    void sacrificeAddsOneGreenMana() {
        harness.addToBattlefield(player1, new HornOfRamos());

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Horn of Ramos");
        harness.assertInGraveyard(player1, "Horn of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability can be activated while Horn of Ramos is tapped")
    void sacrificeAbilityDoesNotRequireUntappedArtifact() {
        harness.addToBattlefield(player1, new HornOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped())
                .isTrue();
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Horn of Ramos");
        harness.assertInGraveyard(player1, "Horn of Ramos");
    }
}
