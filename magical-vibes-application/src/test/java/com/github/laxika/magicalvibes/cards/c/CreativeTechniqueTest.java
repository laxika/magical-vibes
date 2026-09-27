package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreativeTechnique.class, Forest.class, GrizzlyBears.class})
class CreativeTechniqueTest extends BaseCardTest {

    @Test
    void revealsUntilNonlandAndCastsTheCardForFree() {
        harness.setHand(player1, List.of(new CreativeTechnique()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        addManaForCreativeTechnique();

        harness.castSorcery(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof GrizzlyBears
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void demonstrateCreatesCopiesForControllerAndChosenOpponent() {
        harness.setHand(player1, List.of(new CreativeTechnique()));
        addManaForCreativeTechnique();

        harness.castSorcery(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2)
                .extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    private void addManaForCreativeTechnique() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
