package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzekielSimsSpiderTotem.class, GrizzlyBears.class})
class EzekielSimsSpiderTotemTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, a Spider you control gets +2/+2 until end of turn")
    void beginningOfCombatBoostsTargetSpider() {
        Permanent ezekiel = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ezekiel.getId());

        harness.handlePermanentChosen(player1, ezekiel.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ezekiel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ezekiel)).isEqualTo(7);
    }

    @Test
    @DisplayName("The trigger cannot target a non-Spider or an opponent's creature")
    void beginningOfCombatOnlyTargetsSpidersYouControl() {
        Permanent ezekiel = harness.addToBattlefieldAndReturn(player1, new EzekielSimsSpiderTotem());
        Permanent ownNonSpider = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ezekiel.getId())
                .doesNotContain(ownNonSpider.getId(), opponentCreature.getId());
    }

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
