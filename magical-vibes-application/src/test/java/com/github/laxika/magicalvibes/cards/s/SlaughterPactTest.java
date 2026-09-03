package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaughterPact.class, GrizzlyBears.class, DrudgeSkeletons.class})
class SlaughterPactTest extends BaseCardTest {

    private void castPact() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.setHand(player1, List.of(new SlaughterPact()));

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
    }

    private void reachPactUpkeepPrompt() {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.turnNumber = 3;
        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys a target nonblack creature and schedules its upkeep payment")
    void destroysCreatureAndSchedulesPayment() {
        castPact();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        List<PayManaOrLoseGameAtNextUpkeep> scheduled = gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().manaCost()).isEqualTo("{2}{B}");
    }

    @Test
    @DisplayName("Paying {2}{B} at the next upkeep avoids losing the game")
    void payingAvoidsLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Declining the next-upkeep payment loses the game")
    void decliningCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent skeletons = new Permanent(new DrudgeSkeletons());
        gd.playerBattlefields.get(player2.getId()).add(skeletons);
        harness.setHand(player1, List.of(new SlaughterPact()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, skeletons.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }
}
