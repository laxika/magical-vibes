package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornspireVerge.class, Forest.class, Mountain.class})
class ThornspireVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingForRedMana() {
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Green mana ability requires a Mountain or Forest")
    void greenManaRequiresMountainOrForest() {
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountain or a Forest");
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for green mana works while controlling a Mountain")
    void tappingForGreenManaWithMountain() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana works while controlling a Forest")
    void tappingForGreenManaWithForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Mountain does not enable green mana")
    void opponentsMountainDoesNotEnableGreenMana() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    private Permanent addReadyVerge(Player player) {
        return addCreatureReady(player, new ThornspireVerge());
    }

    @Test
    @DisplayName("A tapped Forest still enables green mana")
    void tappedForestEnablesGreenMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled Verge can produce red mana immediately")
    void newlyControlledVergeProducesRedManaImmediately() {
        Permanent verge = harness.addToBattlefieldAndReturn(player1, new ThornspireVerge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the qualifying Forest disables green mana even if it is in the graveyard")
    void losingForestDisablesGreenMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent verge = addReadyVerge(player1);
        harness.activateAbility(player1, 1, 1, null, null);
        verge.untap();
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        harness.setGraveyard(player1, java.util.List.of(forest.getCard()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Verge cannot produce additional mana")
    void tappedVergeCannotProduceAdditionalMana() {
        harness.addToBattlefield(player1, new Mountain());
        addReadyVerge(player1);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
