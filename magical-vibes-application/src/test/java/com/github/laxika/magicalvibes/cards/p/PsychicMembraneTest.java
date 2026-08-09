package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicMembraneTest extends BaseCardTest {

    @Test
    @DisplayName("When Psychic Membrane blocks, accepting the trigger draws a card")
    void acceptingBlockTriggerDrawsCard() {
        addReadyPsychicMembrane(player2);
        addReadyAttacker(player1, new GrizzlyBears());
        setLibrary(player2, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player2.getId()).size();
        declareBlock();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("When Psychic Membrane blocks, declining the trigger draws no card")
    void decliningBlockTriggerDrawsNoCard() {
        addReadyPsychicMembrane(player2);
        addReadyAttacker(player1, new GrizzlyBears());
        setLibrary(player2, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player2.getId()).size();
        declareBlock();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    private Permanent addReadyPsychicMembrane(Player player) {
        Permanent permanent = new Permanent(new PsychicMembrane());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void setLibrary(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
