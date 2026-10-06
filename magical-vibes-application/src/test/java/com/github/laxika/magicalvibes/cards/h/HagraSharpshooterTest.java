package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HagraSharpshooter.class, HedronArchive.class})
class HagraSharpshooterTest extends BaseCardTest {

    private void readySharpshooter() {
        addCreatureReady(player1, new HagraSharpshooter());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("{4}{B}: target creature gets -1/-1 until end of turn")
    void shrinksTargetCreature() {
        readySharpshooter();
        Permanent target = addCreatureReady(player2, new HagraSharpshooter());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The shrink wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        readySharpshooter();
        Permanent target = addCreatureReady(player2, new HagraSharpshooter());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature permanent is an illegal target")
    void rejectsNoncreaturePermanent() {
        readySharpshooter();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new HedronArchive());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can target its own source without tapping it")
    void canTargetItself() {
        readySharpshooter();
        Permanent source = findPermanent(player1, "Hagra Sharpshooter");

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack and put a zero-toughness creature in the graveyard")
    void repeatedActivationsKillTarget() {
        readySharpshooter();
        Permanent target = addCreatureReady(player2, new HagraSharpshooter());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hagra Sharpshooter");
        harness.assertInGraveyard(player2, "Hagra Sharpshooter");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sharpshooter can activate on the opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        readySharpshooter();
        Permanent source = findPermanent(player1, "Hagra Sharpshooter");
        source.tap();
        source.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new HagraSharpshooter());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }
}
