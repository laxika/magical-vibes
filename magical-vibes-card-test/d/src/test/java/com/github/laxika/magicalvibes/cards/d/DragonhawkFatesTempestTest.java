package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.t.TakeOutTheTrash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonhawkFatesTempest.class, AirElemental.class, Mountain.class,
        PullFromEternity.class, TakeOutTheTrash.class})
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

        harness.castFromExile(player1, played.getId());
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

    @Test
    @DisplayName("Counts cards still exiled when the delayed damage resolves")
    void countsStillExiledCardsAtResolution() {
        addDragonhawkAndAirElemental();
        Card removed = new Mountain();
        Card remaining = new Mountain();
        harness.setLibrary(player1, List.of(removed, remaining));
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player2, List.of(new PullFromEternity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, removed.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(removed.getId())).isNull();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Permission to cast exiled cards ends when the next end step begins")
    void cannotCastExiledInstantInNextEndStep() {
        addDragonhawkAndAirElemental();
        Card instant = new TakeOutTheTrash();
        harness.setLibrary(player1, List.of(instant));
        resolveAllTriggers();
        var dragonhawk = findPermanent(player1, "Dragonhawk, Fate's Tempest");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId(), dragonhawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each trigger counts only its own cards and deals damage only once")
    void multipleTriggersTrackSeparateBatches() {
        addDragonhawkAndAirElemental();
        Card first = new Mountain();
        harness.setLibrary(player1, List.of(first));
        resolveAllTriggers();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(second));
        findPermanent(player1, "Dragonhawk, Fate's Tempest").setSummoningSick(false);
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceStep(TurnStep.END_STEP);
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);

        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    private void addDragonhawkAndAirElemental() {
        addCreatureReady(player1, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DragonhawkFatesTempest(), "{3}{R}{R}");
    }
}
