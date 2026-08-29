package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Omnibian.class, GrizzlyBears.class, Forest.class})
class OmnibianTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a target creature a 3/3 Frog until end of turn")
    void makesTargetCreatureAThreeThreeFrog() {
        addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activateOmnibian(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.FROG);
    }

    @Test
    @DisplayName("The Frog and base power and toughness changes expire at end of turn")
    void changesExpireAtEndOfTurn() {
        addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activateOmnibian(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyOmnibian();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void activateOmnibian(Permanent target) {
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyOmnibian() {
        return addCreatureReady(player1, new Omnibian());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
