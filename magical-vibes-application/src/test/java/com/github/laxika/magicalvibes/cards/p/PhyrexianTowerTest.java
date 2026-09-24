package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PhyrexianTower.class)
class PhyrexianTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new PhyrexianTower());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @CardUsed(BlanchwoodTreefolk.class)
    @DisplayName("Tapping and sacrificing a creature adds two black mana")
    void sacrificeCreatureAddsBlackMana() {
        harness.addToBattlefield(player1, new PhyrexianTower());
        addCreatureReady(player1, new BlanchwoodTreefolk());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Blanchwood Treefolk");
        harness.assertInGraveyard(player1, "Blanchwood Treefolk");
    }

    @Test
    @CardUsed(BlanchwoodTreefolk.class)
    @DisplayName("The black-mana ability requires a creature controlled by its activator")
    void requiresCreatureYouControlToSacrifice() {
        harness.addToBattlefield(player1, new PhyrexianTower());
        addCreatureReady(player2, new BlanchwoodTreefolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Blanchwood Treefolk");
    }
}
