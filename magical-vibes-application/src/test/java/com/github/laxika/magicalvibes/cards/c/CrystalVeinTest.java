package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CrystalVein.class)
class CrystalVeinTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsOneColorlessMana() {
        Permanent vein = harness.addToBattlefieldAndReturn(player1, new CrystalVein());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(vein.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Crystal Vein");
    }

    @Test
    @DisplayName("Tap and sacrifice adds two colorless mana and moves the land to the graveyard")
    void sacrificeAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new CrystalVein());

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Crystal Vein");
        harness.assertInGraveyard(player1, "Crystal Vein");
    }

    @Test
    @DisplayName("Both mana abilities require an untapped Crystal Vein")
    void manaAbilitiesRequireUntappedSource() {
        Permanent vein = harness.addToBattlefieldAndReturn(player1, new CrystalVein());
        vein.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Tapping for one mana prevents sacrificing for two without untapping")
    void cannotUseBothManaAbilitiesWithoutUntapping() {
        harness.addToBattlefield(player1, new CrystalVein());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(1);
        harness.assertOnBattlefield(player1, "Crystal Vein");
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice mana ability resolves immediately for its controller")
    void sacrificeManaResolvesImmediatelyForItsController() {
        harness.addToBattlefield(player2, new CrystalVein());

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(2);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isZero();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Crystal Vein");
        harness.assertInGraveyard(player2, "Crystal Vein");
    }
}
