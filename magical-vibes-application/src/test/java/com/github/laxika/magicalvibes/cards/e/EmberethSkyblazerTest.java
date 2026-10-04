package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberethSkyblazer.class, GrizzlyBears.class})
class EmberethSkyblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during your turn only")
    void hasFlyingDuringYourTurnOnly() {
        Permanent skyblazer = addCreatureReady(player1, new EmberethSkyblazer());

        assertThat(gqs.hasKeyword(gd, skyblazer, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, skyblazer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Paying the attack trigger boosts your creatures by the number of opponents")
    void payingOnAttackBoostsOwnCreatures() {
        Permanent skyblazer = addCreatureReady(player1, new EmberethSkyblazer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, skyblazer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, skyblazer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves your creatures unboosted")
    void decliningOnAttackDoesNotBoostOwnCreatures() {
        Permanent skyblazer = addCreatureReady(player1, new EmberethSkyblazer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, skyblazer)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost expires at end of turn")
    void attackBoostExpiresAtEndOfTurn() {
        Permanent skyblazer = addCreatureReady(player1, new EmberethSkyblazer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, skyblazer)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skyblazer)).isEqualTo(4);
    }
}
