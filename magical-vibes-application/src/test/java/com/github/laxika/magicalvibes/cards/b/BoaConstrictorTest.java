package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoaConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +3/+3 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent constrictor = addReadyConstrictor(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(constrictor.getPowerModifier()).isEqualTo(3);
        assertThat(constrictor.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability taps Boa Constrictor")
    void abilityTapsConstrictor() {
        Permanent constrictor = addReadyConstrictor(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(constrictor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent constrictor = addReadyConstrictor(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(constrictor.getPowerModifier()).isZero();
        assertThat(constrictor.getToughnessModifier()).isZero();
    }

    private Permanent addReadyConstrictor(Player player) {
        Permanent perm = new Permanent(new BoaConstrictor());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
