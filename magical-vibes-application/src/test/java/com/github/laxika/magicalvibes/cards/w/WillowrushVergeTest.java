package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillowrushVerge.class, Forest.class, Island.class})
class WillowrushVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueMana() {
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Green mana ability requires a Forest or Island")
    void greenManaRequiresForestOrIsland() {
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest or an Island");
        assertThat(verge.isTapped()).isFalse();
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
    @DisplayName("Tapping for green mana works while controlling an Island")
    void tappingForGreenManaWithIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Forest does not enable green mana")
    void opponentsForestDoesNotEnableGreenMana() {
        harness.addToBattlefield(player2, new Forest());
        Permanent verge = addReadyVerge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Island still enables green mana")
    void tappedIslandEnablesGreenMana() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        Permanent verge = addReadyVerge(player1);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered Verge can immediately produce blue mana")
    void newlyEnteredVergeCanProduceMana() {
        Permanent verge = harness.enterBattlefieldAndReturn(player1, new WillowrushVerge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the enabling Forest disables green mana but leaves blue available")
    void losingForestDisablesOnlyGreenMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent verge = addReadyVerge(player1);
        harness.activateAbility(player1, 1, 1, null, null);
        verge.untap();
        gd.playerBattlefields.get(player1.getId()).remove(forest);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(verge.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Verge cannot activate either mana ability")
    void tappedVergeCannotProduceMoreMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent verge = addReadyVerge(player1);
        verge.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addReadyVerge(Player player) {
        Permanent verge = harness.addToBattlefieldAndReturn(player, new WillowrushVerge());
        verge.setSummoningSick(false);
        return verge;
    }
}
