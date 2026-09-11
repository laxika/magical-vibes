package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LakeTownToymaker.class, GrizzlyBears.class, Island.class})
class LakeTownToymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat boosts another creature and grants first strike after two draws")
    void boostsAnotherCreatureAfterTwoDraws() {
        harness.addToBattlefield(player1, new LakeTownToymaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        drawTwoCards(player1);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger unless its controller has drawn two cards")
    void doesNotTriggerBeforeTwoDraws() {
        harness.addToBattlefield(player1, new LakeTownToymaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only another creature controlled by its controller")
    void targetsOnlyAnotherCreatureYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LakeTownToymaker());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        drawTwoCards(player1);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId())
                .doesNotContain(source.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void buffsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new LakeTownToymaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        drawTwoCards(player1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void drawTwoCards(Player player) {
        harness.setLibrary(player, List.of(new Island(), new Island()));
        drawCard(player);
        drawCard(player);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
