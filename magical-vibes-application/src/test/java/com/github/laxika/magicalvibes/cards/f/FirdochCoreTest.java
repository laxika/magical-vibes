package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirdochCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Firdoch Core adds one mana of the chosen color")
    void tappingAddsChosenMana() {
        Permanent core = addReadyCore(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(core.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four mana makes Firdoch Core a 4/4 creature")
    void payingFourManaAnimatesCore() {
        Permanent core = addReadyCore(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, core)).isTrue();
        assertThat(gqs.getEffectivePower(gd, core)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, core)).isEqualTo(4);
    }

    @Test
    @DisplayName("Firdoch Core stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent core = addReadyCore(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, core)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(core.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, core)).isFalse();
    }

    private Permanent addReadyCore(Player player) {
        Permanent permanent = new Permanent(new FirdochCore());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
