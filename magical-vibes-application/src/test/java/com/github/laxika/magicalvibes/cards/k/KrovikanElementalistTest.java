package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({KrovikanElementalist.class, BalduvianBears.class, Plains.class})
class KrovikanElementalistTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{R} gives target creature +1/+0 until end of turn")
    void pumpsTargetCreature() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        int basePower = gqs.getEffectivePower(gd, bears);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("{2}{R} can target an opponent's creature")
    void pumpsOpponentsCreature() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        int basePower = gqs.getEffectivePower(gd, opponentBears);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("{2}{R} cannot target a noncreature permanent")
    void pumpCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{U}{U} cannot target a noncreature permanent")
    void flyingCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{U}{U} grants flying to a creature you control")
    void grantsFlyingToControlledCreature() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("{U}{U} flying grant expires at end of turn")
    void flyingGrantExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{U}{U} sacrifices the target at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Balduvian Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("{U}{U} cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
