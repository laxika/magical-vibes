package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrelsOfBlastingJelly.class, GrizzlyBears.class})
class BarrelsOfBlastingJellyTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and can be activated only once each turn")
    void manaAbilityAddsManaOnlyOnceEachTurn() {
        Permanent jelly = harness.addToBattlefieldAndReturn(player1, new BarrelsOfBlastingJelly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        jelly.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 5 damage to target creature")
    void sacrificesItselfAndDealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Barrels of Blasting Jelly");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
