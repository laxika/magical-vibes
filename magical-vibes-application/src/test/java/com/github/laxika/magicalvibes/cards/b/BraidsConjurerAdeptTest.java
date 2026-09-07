package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidsConjurerAdept.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class, Opt.class})
class BraidsConjurerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may put an artifact, creature, or land from hand onto the battlefield")
    void activePlayerMayPutEligibleCardFromHandOntoBattlefield() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Opt();
        harness.setHand(player2, List.of(artifact, creature, land, instant));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0, 1, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(artifact, land, instant);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getCard()))
                .contains(creature);
    }

    @Test
    @DisplayName("Declining the upkeep choice leaves the active player's hand unchanged")
    void decliningLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new BraidsConjurerAdept());
        Card creature = new GrizzlyBears();
        harness.setHand(player2, List.of(creature));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getCard()))
                .doesNotContain(creature);
    }
}
