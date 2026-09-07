package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardosTechnique.class, LlanowarElves.class, GrizzlyBears.class, HillGiant.class})
class LeonardosTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one or two eligible creature cards from the graveyard")
    void returnsOneOrTwoEligibleCreatures() {
        Card first = new LlanowarElves();
        Card second = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        LeonardosTechnique technique = new LeonardosTechnique();
        harness.setGraveyard(player1, List.of(first, second, tooExpensive));
        harness.setHand(player1, List.of(technique));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId(), technique.getId());
    }

    @Test
    @DisplayName("Cannot choose an ineligible creature card")
    void cannotChooseHighManaValueCreature() {
        Card eligible = new LlanowarElves();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        harness.setHand(player1, List.of(new LeonardosTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(tooExpensive.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and resolves the reanimation")
    void sneaksAndReturnsCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card returned = new LlanowarElves();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new LeonardosTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }
}
