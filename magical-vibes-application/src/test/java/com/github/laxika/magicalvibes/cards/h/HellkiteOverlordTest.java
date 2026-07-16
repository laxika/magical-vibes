package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HellkiteOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("{R} ability gives +1/+0 until end of turn")
    void firebreathingBoostsPower() {
        Permanent overlord = addReadyOverlord(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(1);
        assertThat(overlord.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{R} boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent overlord = addReadyOverlord(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(0);
        assertThat(overlord.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{B}{G} ability grants a regeneration shield")
    void regenerationGrantsShield() {
        Permanent overlord = addReadyOverlord(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addReadyOverlord(Player player) {
        HellkiteOverlord card = new HellkiteOverlord();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
