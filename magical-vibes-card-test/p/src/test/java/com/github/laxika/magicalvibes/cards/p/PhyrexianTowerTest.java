package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianTower.class, BlanchwoodTreefolk.class})
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

    @Test
    @DisplayName("A tapped creature with summoning sickness can be sacrificed for mana")
    void canSacrificeTappedSummoningSickCreature() {
        harness.addToBattlefield(player1, new PhyrexianTower());
        var creature = harness.addToBattlefieldAndReturn(player1, new BlanchwoodTreefolk());
        creature.setSummoningSick(true);
        creature.tap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Blanchwood Treefolk");
        harness.assertInGraveyard(player1, "Blanchwood Treefolk");
    }

    @Test
    @DisplayName("A tapped Tower cannot sacrifice a creature for mana")
    void tappedTowerCannotPaySacrificeAbilityCost() {
        var tower = harness.addToBattlefieldAndReturn(player1, new PhyrexianTower());
        tower.tap();
        harness.addToBattlefield(player1, new BlanchwoodTreefolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertOnBattlefield(player1, "Blanchwood Treefolk");
        harness.assertNotInGraveyard(player1, "Blanchwood Treefolk");
    }

    @Test
    @DisplayName("The controller chooses which creature to sacrifice when several are available")
    void choosesOneCreatureToSacrifice() {
        harness.addToBattlefield(player1, new PhyrexianTower());
        var kept = harness.addToBattlefieldAndReturn(player1, new BlanchwoodTreefolk());
        var sacrificed = harness.addToBattlefieldAndReturn(player1, new BlanchwoodTreefolk());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kept).doesNotContain(sacrificed);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sacrificed.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
