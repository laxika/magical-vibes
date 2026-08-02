package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnToTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses exactly X creature cards with mana value 2 or less from your graveyard")
    void choosesEligibleTargets() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card hillGiant = new HillGiant();
        Card plains = new Plains();
        Card opponentBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, elves, hillGiant, plains));
        harness.setGraveyard(player2, List.of(opponentBears));
        harness.setHand(player1, List.of(new ReturnToTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId());
        assertThat(choice.validCardIds()).doesNotContain(hillGiant.getId(), plains.getId(), opponentBears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
        assertThat(gd.stack.getFirst().getTargetCardIds()).containsExactly(bears.getId(), elves.getId());
    }

    @Test
    @DisplayName("Returns the selected creature cards to the battlefield")
    void returnsSelectedCreatures() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new ReturnToTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Return to the Ranks");
    }

    @Test
    @DisplayName("Cards returned together do not apply their entry replacement effects to each other")
    void returnedCardsEnterSimultaneously() {
        Card paragon = new BramblewoodParagon();
        Card warrior = new KjeldoranWarrior();
        harness.setGraveyard(player1, List.of(paragon, warrior));
        harness.setHand(player1, List.of(new ReturnToTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(paragon.getId(), warrior.getId()));
        harness.passBothPriorities();

        Permanent returnedWarrior = findPermanent(player1, "Kjeldoran Warrior");
        assertThat(returnedWarrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("X greater than the eligible creature count is illegal")
    void xGreaterThanEligibleCountThrows() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Plains()));
        harness.setHand(player1, List.of(new ReturnToTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching creature cards in graveyard");
    }

    @Test
    @DisplayName("X=0 resolves without returning cards")
    void xZeroReturnsNothing() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new ReturnToTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Return to the Ranks");
    }
}
