package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialSword.class, BalduvianBears.class})
class CelestialSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature you control +3/+3 until end of turn")
    void pumpsTargetCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new CelestialSword());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 3);
        assertThat(sword.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target creature is sacrificed at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefieldAndReturn(player1, new CelestialSword());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Balduvian Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new CelestialSword());
        Permanent opponentBears = addCreatureReady(player2, new BalduvianBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new CelestialSword());
        Permanent otherSword = harness.addToBattlefieldAndReturn(player1, new CelestialSword());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherSword.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
