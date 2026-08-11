package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AndraditeLeechTest extends BaseCardTest {

    @Test
    @DisplayName("Black spells cast by the controller cost one additional mana")
    void controllerBlackSpellsCostMore() {
        harness.addToBattlefield(player1, new AndraditeLeech());
        harness.setHand(player1, List.of(new BlackKnight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Nonblack spells cast by the controller are not taxed")
    void controllerNonblackSpellsAreNotTaxed() {
        harness.addToBattlefield(player1, new AndraditeLeech());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's black spells are not taxed")
    void opponentBlackSpellsAreNotTaxed() {
        harness.addToBattlefield(player1, new AndraditeLeech());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BlackKnight()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability gives this creature +1/+1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent leech = addCreatureReady(player1, new AndraditeLeech());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leech.getPowerModifier()).isEqualTo(1);
        assertThat(leech.getToughnessModifier()).isEqualTo(1);
    }
}
