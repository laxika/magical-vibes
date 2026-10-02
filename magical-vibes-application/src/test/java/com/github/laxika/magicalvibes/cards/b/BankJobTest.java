package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BankJob.class, Forest.class, GrizzlyBears.class})
class BankJobTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the bottom creature card and lets it be cast this turn")
    void exilesBottomCreatureCard() {
        Card top = new Forest();
        Card creature = new GrizzlyBears();
        Card bottomNonCreature = new Forest();
        harness.addToBattlefield(player1, new BankJob());
        harness.setLibrary(player1, List.of(top, creature, bottomNonCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottomNonCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.exilePlayPermissions).containsEntry(creature.getId(), player1.getId());
    }

    @Test
    @DisplayName("Puts the uncast creature into its owner's graveyard and creates a Treasure")
    void uncastCreatureCreatesTreasureAtNextEndStep() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new BankJob());
        harness.setLibrary(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(StepTriggerService.class)
                        .handleEndStepTriggers(gd));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when the exiled creature is cast")
    void castCreatureAvoidsDelayedTreasure() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new BankJob());
        harness.setLibrary(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCardFromExile(gd, player1, creature.getId(), null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(StepTriggerService.class)
                        .handleEndStepTriggers(gd));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
