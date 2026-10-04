package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarrisonGriffin.class, YouthfulKnight.class, GrizzlyBears.class})
class GarrisonGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets the trigger target a Knight you control")
    void attackTriggerTargetsKnightYouControl() {
        addCreatureReady(player1, new GarrisonGriffin());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        Permanent opponentKnight = addCreatureReady(player2, new YouthfulKnight());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(knight.getId());
        assertThat(choice.validIds()).doesNotContain(opponentKnight.getId());
    }

    @Test
    @DisplayName("The attack trigger grants flying until end of turn")
    void attackTriggerGrantsFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new GarrisonGriffin());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, knight.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target a non-Knight or an opponent's Knight")
    void attackTriggerRejectsIllegalTargets() {
        addCreatureReady(player1, new GarrisonGriffin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentKnight = addCreatureReady(player2, new YouthfulKnight());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentKnight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability does not trigger when Garrison Griffin does not attack")
    void doesNotTriggerWhenGarrisonGriffinDoesNotAttack() {
        addCreatureReady(player1, new GarrisonGriffin());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(knight.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
