package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DamageForCardsStillExiledAtNextEndStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonhawkFatesTempest.class, AirElemental.class, Mountain.class})
class DragonhawkFatesTempestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card for each controlled creature with power 4 or greater")
    void exilesCardsForPowerFourCreatures() {
        addDragonhawkAndAirElemental();
        Card first = new Mountain();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.getDelayedActions(DamageForCardsStillExiledAtNextEndStep.class))
                .hasSize(1);
    }

    @Test
    @DisplayName("Exiles cards when it attacks")
    void exilesCardsWhenItAttacks() {
        addCreatureReady(player1, new DragonhawkFatesTempest());
        addCreatureReady(player1, new AirElemental());
        Card card = new Mountain();
        harness.setLibrary(player1, List.of(card));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(card.getId());
    }

    @Test
    @DisplayName("Deals damage only for cards from the trigger that remain exiled")
    void damagesForCardsStillExiled() {
        addDragonhawkAndAirElemental();
        Card played = new Mountain();
        Card unplayed = new Mountain();
        harness.setLibrary(player1, List.of(played, unplayed));
        resolveAllTriggers();

        gs.playCardFromExile(gd, player1, played.getId(), null, null);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DamageForCardsStillExiledAtNextEndStep.class)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(unplayed.getId());
    }

    private void addDragonhawkAndAirElemental() {
        addCreatureReady(player1, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DragonhawkFatesTempest(), "{3}{R}{R}");
    }
}
