package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaxterStockman.class, Memnite.class, GrizzlyBears.class})
class BaxterStockmanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 1/1 colorless Robot artifact creature token")
    void enteringCreatesRobotToken() {
        harness.setHand(player1, List.of(new BaxterStockman()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().getPower()).isEqualTo(1);
        assertThat(robot.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Beginning of combat boosts a controlled artifact creature and grants first strike and vigilance")
    void beginningOfCombatBoostsTargetArtifactCreature() {
        addBaxter(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Memnite());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId())
                .doesNotContain(nonArtifactCreature.getId(), opponentArtifactCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonArtifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonArtifactCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentArtifactCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The beginning-of-combat boost wears off at end of turn")
    void beginningOfCombatBoostWearsOffAtEndOfTurn() {
        addBaxter(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Memnite());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addBaxter(player1);
        harness.addToBattlefield(player1, new Memnite());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addBaxter(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new BaxterStockman());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
