package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeapingLizardTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{G}: Leaping Lizard gets -0/-1 and gains flying")
    void activationGrantsFlyingAndShrinks() {
        Permanent lizard = addReadyLizard(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations stack the toughness reduction")
    void repeatedActivationsStack() {
        Permanent lizard = addReadyLizard(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying and the -0/-1 wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent lizard = addReadyLizard(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lizard, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(3);
    }

    private Permanent addReadyLizard(Player player) {
        Permanent perm = new Permanent(new LeapingLizard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
