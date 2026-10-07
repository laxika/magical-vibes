package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
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

@CardUsed({SplitDecision.class, Counterspell.class, Divination.class, Forest.class, GrizzlyBears.class, TheValeyard.class})
class SplitDecisionTest extends BaseCardTest {

    @BeforeEach
    void setUpCastingTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void denialMajorityCountersTargetSpell() {
        harness.castFromHand(player2, new Divination(), "{2}{U}");
        UUID targetId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

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

        harness.castFromHand(player2, new Divination(), "{2}{U}");
        UUID targetId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DUPLICATION);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void cannotTargetPermanentSpell() {
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        UUID permanentSpellId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanentSpellId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void duplicationMajorityCopiesSpellWithoutCounteringOriginal() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second, new Forest()));

        harness.castFromHand(player2, new Divination(), "{2}{U}");
        UUID originalId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, originalId);
        harness.handleListChoice(player1, ChoiceContext.VoteForDenialOrDuplicationChoice.DUPLICATION);
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DUPLICATION);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(originalId);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(originalId);
    }

    @Test
    void copiedInstantCanRetargetAndCounterTheOriginalInstant() {
        harness.castFromHand(player2, new Divination(), "{2}{U}");
        UUID sorceryId = gd.stack.getLast().getTargetableId();

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, sorceryId);
        UUID counterspellId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, counterspellId);
        harness.handleListChoice(player1, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DUPLICATION);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, counterspellId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof Counterspell);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(sorceryId);
    }

    @Test
    void opponentCanUseValeyardsAdditionalVoteToMakeDenialWin() {
        harness.addToBattlefield(player2, new TheValeyard());
        harness.castFromHand(player2, new Divination(), "{2}{U}");
        UUID originalId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SplitDecision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, originalId);
        harness.handleListChoice(player1, ChoiceContext.VoteForDenialOrDuplicationChoice.DUPLICATION);
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.VoteForDenialOrDuplicationChoice.DENIAL);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof Divination);
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
