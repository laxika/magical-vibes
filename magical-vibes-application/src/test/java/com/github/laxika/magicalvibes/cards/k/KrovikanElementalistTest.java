package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrovikanElementalist.class, BalduvianBears.class, Plains.class, RayOfCommand.class})
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

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Balduvian Bears");
        assertThat(gd.stack).hasSize(1);
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

    @Test
    @DisplayName("Repeated pump activations stack without increasing toughness or tapping the source")
    void repeatedPumpActivationsStack() {
        Permanent elementalist = harness.addToBattlefieldAndReturn(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
        assertThat(elementalist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating during the end step delays sacrifice until the following end step")
    void endStepActivationWaitsForFollowingEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertOnBattlefield(player1, "Balduvian Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Balduvian Bears");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("The delayed sacrifice cannot sacrifice a creature now controlled by an opponent")
    void doesNotSacrificeCreatureControlledByOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addToBattlefield(player1, new KrovikanElementalist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertNotInGraveyard(player1, "Balduvian Bears");
    }
}
