package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelimTorsEdict.class, BayFalcon.class, RayOfCommand.class})
class TelimTorsEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a permanent you control and schedules a draw at the next upkeep")
    void exilesOwnPermanentAndSchedulesDraw() {
        harness.addToBattlefield(player1, new BayFalcon());
        harness.setHand(player1, List.of(new TelimTorsEdict()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Bay Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bay Falcon"));

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.addToBattlefield(player1, new BayFalcon());
        harness.setHand(player1, List.of(new TelimTorsEdict()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Bay Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

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

    @Test
    @DisplayName("Cannot target a permanent an opponent owns and controls")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player2, new BayFalcon());
        harness.setHand(player1, List.of(new TelimTorsEdict()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a permanent you control but do not own")
    void canTargetPermanentControlledButNotOwned() {
        Permanent target = addCreatureReady(player2, new BayFalcon());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player1, List.of(new TelimTorsEdict()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Bay Falcon"));
    }

    @Test
    @DisplayName("Can target a permanent you own but do not control")
    void canTargetPermanentOwnedButNotControlled() {
        harness.forceActivePlayer(player2);
        Permanent target = addCreatureReady(player1, new BayFalcon());
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.setHand(player1, List.of(new TelimTorsEdict()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bay Falcon"));
    }
}
