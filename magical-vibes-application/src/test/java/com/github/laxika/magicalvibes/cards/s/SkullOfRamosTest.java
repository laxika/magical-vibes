package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SkullOfRamos.class)
class SkullOfRamosTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one black mana")
    void tapAddsOneBlackMana() {
        harness.addToBattlefield(player1, new SkullOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skull of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability adds one black mana and moves the artifact to the graveyard")
    void sacrificeAddsOneBlackMana() {
        harness.addToBattlefield(player1, new SkullOfRamos());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Skull of Ramos");
        harness.assertInGraveyard(player1, "Skull of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability can be activated while Skull of Ramos is tapped")
    void sacrificeAbilityDoesNotRequireUntappedArtifact() {
        harness.addToBattlefield(player1, new SkullOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Skull of Ramos");
        harness.assertInGraveyard(player1, "Skull of Ramos");
    }

    @Test
    @DisplayName("Tap ability taps the artifact and cannot be activated again while tapped")
    void cannotTapTwiceWithoutUntapping() {
        harness.addToBattlefield(player1, new SkullOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both mana abilities resolve immediately and add mana to their controller's pool")
    void manaAbilitiesAddManaToOtherPlayersPoolWithoutUsingStack() {
        harness.addToBattlefield(player2, new SkullOfRamos());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Skull of Ramos");
        harness.assertInGraveyard(player2, "Skull of Ramos");
    }
}
