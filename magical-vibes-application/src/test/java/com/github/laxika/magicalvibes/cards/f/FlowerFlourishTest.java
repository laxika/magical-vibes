package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlowerFlourishTest extends BaseCardTest {

    @Test
    @DisplayName("Flower searches for a basic Forest or Plains card")
    void flowerSearchesForForestOrPlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(forest, plains, island, bears);
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(forest)));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, island, bears);
    }

    @Test
    @DisplayName("Flourish pumps only creatures the caster controls until end of turn")
    void flourishPumpsOwnCreaturesUntilEndOfTurn() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(ownBears.getEffectivePower()).isEqualTo(4);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingBears.getEffectivePower()).isEqualTo(2);
        assertThat(opposingBears.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBears.getEffectivePower()).isEqualTo(2);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(2);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
