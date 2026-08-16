package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GixianPuppeteerTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers on the second card drawn each turn")
    void triggersOnSecondCardDrawn() {
        harness.addToBattlefield(player1, new GixianPuppeteer());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Dies and returns a target creature card with mana value 3 or less")
    void diesReturnsTargetCheapCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.handleMultipleCardsChosen(player1, List.of(targetId));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Gixian Puppeteer");
    }

    @Test
    @DisplayName("Does not trigger when the graveyard has no eligible target")
    void diesWithoutEligibleTarget() {
        harness.setGraveyard(player1, List.of(new HillGiant()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private void killInCombat() {
        Permanent puppeteer = new Permanent(new GixianPuppeteer());
        puppeteer.setSummoningSick(false);
        puppeteer.setBlocking(true);
        puppeteer.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(puppeteer);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
