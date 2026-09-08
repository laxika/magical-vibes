package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZukoConflicted.class, Forest.class})
class ZukoConflictedTest extends BaseCardTest {

    @Test
    void drawsACardAsChosenMode() {
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        addZuko(player1);

        advanceToPrecombatMain(player1);
        harness.handleListChoice(player1, "Draw a card");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void putsACounterOnItselfAsChosenMode() {
        Permanent zuko = addZuko(player1);

        advanceToPrecombatMain(player1);
        harness.handleListChoice(player1, "Put a +1/+1 counter on Zuko");
        harness.passBothPriorities();

        assertThat(zuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void addsRedManaAsChosenMode() {
        addZuko(player1);

        advanceToPrecombatMain(player1);
        harness.handleListChoice(player1, "Add {R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void returnsUnderAnOpponentsControlAsChosenMode() {
        addZuko(player1);

        advanceToPrecombatMain(player1);
        harness.handleListChoice(player1,
                "Exile Zuko, then return him to the battlefield under an opponent's control");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ZukoConflicted);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ZukoConflicted);
    }

    @Test
    void chosenModeIsNotOfferedAgain() {
        addZuko(player1);

        advanceToPrecombatMain(player1);
        harness.handleListChoice(player1, "Add {R}");
        harness.passBothPriorities();

        advanceToPrecombatMain(player1);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain("Add {R}");
        assertThat(choice.options()).contains("Draw a card");
    }

    private Permanent addZuko(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ZukoConflicted());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
