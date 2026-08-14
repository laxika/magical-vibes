package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChimericCoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Activation makes Chimeric Coils an X/X Construct artifact creature until end of turn")
    void activationAnimatesWithChosenX() {
        Permanent coils = addCoilsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThat(gqs.isCreature(gd, coils)).isFalse();

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, coils)).isTrue();
        assertThat(gqs.getEffectivePower(gd, coils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, coils)).isEqualTo(3);
        assertThat(coils.getTransientSubtypes()).contains(CardSubtype.CONSTRUCT);
    }

    @Test
    @DisplayName("Activation sacrifices Chimeric Coils at the next end step")
    void activationSchedulesSacrificeAtNextEndStep() {
        addCoilsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chimeric Coils");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chimeric Coils");
    }

    private Permanent addCoilsReady(Player player) {
        ChimericCoils card = new ChimericCoils();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
