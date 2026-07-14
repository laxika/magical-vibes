package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature you control +2/+2 until end of turn")
    void pumpsTargetCreature() {
        Permanent mask = new Permanent(new DragonMask());
        mask.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mask);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Target creature is returned to its owner's hand at the beginning of the next end step")
    void returnsTargetToHandAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent mask = new Permanent(new DragonMask());
        mask.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mask);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Still on the battlefield during the main phase.
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Advance to the end step — the creature should be bounced to its owner's hand.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent mask = new Permanent(new DragonMask());
        mask.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mask);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
