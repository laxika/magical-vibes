package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AhnCropInvader.class, GrizzlyBears.class})
class AhnCropInvaderTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn only")
    void hasFirstStrikeDuringItsControllersTurnOnly() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, invader, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, invader, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing another creature gives it +2/+0 until end of turn")
    void sacrificesAnotherCreatureAndGetsBoost() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, invader);
        int baseToughness = gqs.getEffectiveToughness(gd, invader);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, invader)).isEqualTo(baseToughness);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(invader).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent invader = addCreatureReady(player1, new AhnCropInvader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, invader);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invader)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new AhnCropInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
