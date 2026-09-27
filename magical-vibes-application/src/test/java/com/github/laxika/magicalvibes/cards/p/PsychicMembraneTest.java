package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychicMembrane.class, AlphaMyr.class, Forest.class})
class PsychicMembraneTest extends BaseCardTest {

    @Test
    @DisplayName("When Psychic Membrane blocks, accepting the trigger draws a card")
    void acceptingBlockTriggerDrawsCard() {
        addCreatureReady(player2, new PsychicMembrane());
        addCreatureReady(player1, new AlphaMyr());
        harness.setLibrary(player2, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player2.getId()).size();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("When Psychic Membrane blocks, declining the trigger draws no card")
    void decliningBlockTriggerDrawsNoCard() {
        addCreatureReady(player2, new PsychicMembrane());
        addCreatureReady(player1, new AlphaMyr());
        harness.setLibrary(player2, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player2.getId()).size();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("When another creature blocks, Psychic Membrane's trigger does not trigger")
    void doesNotTriggerWhenAnotherCreatureBlocks() {
        addCreatureReady(player2, new PsychicMembrane());
        addCreatureReady(player2, new AlphaMyr());
        addCreatureReady(player1, new AlphaMyr());
        harness.setLibrary(player2, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player2.getId()).size();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }
}
