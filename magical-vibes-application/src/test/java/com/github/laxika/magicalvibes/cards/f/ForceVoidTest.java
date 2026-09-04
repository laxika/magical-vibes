package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForceVoid.class, KjeldoranWarrior.class})
class ForceVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and schedules a draw when the controller cannot pay {1}")
    void countersAndSchedulesDrawWhenOpponentCannotPay() {
        KjeldoranWarrior warrior = new KjeldoranWarrior();
        harness.castFromHand(player1, warrior, "{W}");

        harness.setHand(player2, List.of(new ForceVoid()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kjeldoran Warrior");
        harness.assertNotOnBattlefield(player1, "Kjeldoran Warrior");

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spell resolves but still schedules a draw when the opponent pays {1}")
    void schedulesDrawButSpellNotCounteredWhenOpponentPays() {
        KjeldoranWarrior warrior = new KjeldoranWarrior();
        harness.castFromHand(player1, warrior, "{W}");
        harness.addMana(player1, ManaColor.WHITE, 1); // 1 to pay

        harness.setHand(player2, List.of(new ForceVoid()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Warrior");
    }

    @Test
    @DisplayName("Force Void's controller draws at the next upkeep")
    void controllerDrawsAtNextUpkeep() {
        KjeldoranWarrior warrior = new KjeldoranWarrior();
        harness.castFromHand(player1, warrior, "{W}");

        harness.setHand(player2, List.of(new ForceVoid()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passBothPriorities();

        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int controllerDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int targetControllerHandBefore = gd.playerHands.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(targetControllerHandBefore);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Fizzles without scheduling a draw if the target spell leaves the stack")
    void fizzlesWithoutSchedulingDrawWhenTargetLeavesStack() {
        KjeldoranWarrior warrior = new KjeldoranWarrior();
        harness.castFromHand(player1, warrior, "{W}");

        harness.setHand(player2, List.of(new ForceVoid()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(warrior.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Force Void");
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
