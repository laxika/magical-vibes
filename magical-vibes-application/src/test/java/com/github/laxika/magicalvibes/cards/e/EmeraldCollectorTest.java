package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeraldCollector.class, GrizzlyBears.class})
class EmeraldCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it deals combat damage to a player")
    void drawsOnCombatDamage() {
        Permanent collector = addCreatureReady(player1, new EmeraldCollector());
        collector.setAttacking(true);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Conjures Mox Emerald on the controller's third draw")
    void conjuresOnThirdDrawOnlyOnce() {
        harness.addToBattlefield(player1, new EmeraldCollector());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        harness.assertInHand(player1, "Mox Emerald");
        int handSizeAfterThirdDraw = gd.playerHands.get(player1.getId()).size();

        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterThirdDraw + 1);
    }

    @Test
    @DisplayName("Sets its base power and toughness to 4/4 until end of turn")
    void setsBasePowerAndToughnessUntilEndOfTurn() {
        Permanent collector = addCreatureReady(player1, new EmeraldCollector());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, collector)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, collector)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, collector)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, collector)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
