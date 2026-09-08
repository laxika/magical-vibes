package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Crazed Armodon")
class CrazedArmodonTest extends BaseCardTest {

    private Permanent addArmodon() {
        Permanent armodon = new Permanent(new CrazedArmodon());
        armodon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(armodon);
        return armodon;
    }

    @Test
    @DisplayName("Activating gives +3/+0 and trample")
    void activationBoostsAndGrantsTrample() {
        Permanent armodon = addArmodon();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armodon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, armodon)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("It is destroyed at the beginning of the next end step")
    void destroyedAtNextEndStep() {
        addArmodon();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Crazed Armodon");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crazed Armodon");
        harness.assertInGraveyard(player1, "Crazed Armodon");
    }

    @Test
    @DisplayName("Cannot activate a second time in the same turn")
    void cannotActivateTwiceInOneTurn() {
        addArmodon();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Without activating, it survives the end step")
    void survivesEndStepWithoutActivation() {
        addArmodon();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crazed Armodon");
    }
}
