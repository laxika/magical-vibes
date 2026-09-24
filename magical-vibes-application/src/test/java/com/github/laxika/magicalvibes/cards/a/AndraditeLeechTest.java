package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Duskwalker;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AndraditeLeech.class, Duskwalker.class, GoblinSpy.class})
class AndraditeLeechTest extends BaseCardTest {

    @Test
    @DisplayName("Black spells cast by the controller require an additional black mana")
    void controllerBlackSpellsRequireAdditionalBlackMana() {
        harness.addToBattlefield(player1, new AndraditeLeech());
        harness.setHand(player1, List.of(new Duskwalker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The controller can cast a black spell with the additional black mana")
    void controllerBlackSpellsCanPayAdditionalBlackMana() {
        harness.addToBattlefield(player1, new AndraditeLeech());

        harness.castFromHand(player1, new Duskwalker(), "{B}{B}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Nonblack spells cast by the controller are not taxed")
    void controllerNonblackSpellsAreNotTaxed() {
        harness.addToBattlefield(player1, new AndraditeLeech());

        harness.castFromHand(player1, new GoblinSpy(), "{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's black spells are not taxed")
    void opponentBlackSpellsAreNotTaxed() {
        harness.addToBattlefield(player1, new AndraditeLeech());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Duskwalker(), "{B}");

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

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void activatedAbilityBoostExpiresAtEndOfTurn() {
        Permanent leech = addCreatureReady(player1, new AndraditeLeech());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(leech.getPowerModifier()).isZero();
        assertThat(leech.getToughnessModifier()).isZero();
    }
}
