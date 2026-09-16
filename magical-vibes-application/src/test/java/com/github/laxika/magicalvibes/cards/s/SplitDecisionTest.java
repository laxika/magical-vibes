package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplitDecision.class, Divination.class, Forest.class, GrizzlyBears.class})
class SplitDecisionTest extends BaseCardTest {

    @BeforeEach
    void setUpCastingTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void denialMajorityCountersTargetSpell() {
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0);
        UUID targetId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);

        assertThat(gd.stack).hasSize(0);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Divination"));
    }

    @Test
    void duplicationWinsTieAndCopiesTargetSpell() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0);
        UUID targetId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DUPLICATION);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void cannotTargetPermanentSpell() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        UUID permanentSpellId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanentSpellId))
                .isInstanceOf(IllegalStateException.class);
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.VoteForDenialOrDuplicationChoice.OPTIONS);
        return choice;
    }
}
