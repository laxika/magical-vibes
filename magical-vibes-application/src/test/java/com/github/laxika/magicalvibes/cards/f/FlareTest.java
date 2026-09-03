package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flare.class, GrizzlyBears.class, GarrukWildspeaker.class})
class FlareTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target player and schedules a draw at the next upkeep")
    void deals1DamageToPlayerAndSchedulesDraw() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.RED, 3);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void deals1DamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        // 1 damage does not destroy a 2/2, which survives on the battlefield.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 1 damage to a planeswalker")
    void deals1DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Flare()));
        harness.addMana(player1, ManaColor.RED, 3);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
