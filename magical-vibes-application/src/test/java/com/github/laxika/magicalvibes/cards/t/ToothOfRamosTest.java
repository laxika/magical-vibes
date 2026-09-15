package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ToothOfRamos.class)
class ToothOfRamosTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one white mana")
    void tapAddsOneWhiteMana() {
        harness.addToBattlefield(player1, new ToothOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Tooth of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability adds one white mana and moves the artifact to the graveyard")
    void sacrificeAddsOneWhiteMana() {
        harness.addToBattlefield(player1, new ToothOfRamos());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Tooth of Ramos");
        harness.assertInGraveyard(player1, "Tooth of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability can be activated while Tooth of Ramos is tapped")
    void sacrificeAbilityDoesNotRequireUntappedArtifact() {
        harness.addToBattlefield(player1, new ToothOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Tooth of Ramos");
        harness.assertInGraveyard(player1, "Tooth of Ramos");
    }
}
