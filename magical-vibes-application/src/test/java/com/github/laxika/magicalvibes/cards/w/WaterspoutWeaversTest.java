package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.InkDissolver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterspoutWeavers.class, ElvishWarrior.class, InkDissolver.class})
class WaterspoutWeaversTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing the shared-type card gives flying to each creature you control")
    void revealGrantsFlyingToAll() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        Permanent opponentElf = addCreatureReady(player2, new ElvishWarrior());
        harness.setLibrary(player1, List.of(new InkDissolver())); // shares Merfolk/Wizard

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, weavers, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The granted flying wears off at cleanup")
    void flyingWearsOffAtEndOfTurn() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        harness.setLibrary(player1, List.of(new InkDissolver()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, weavers, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal grants no flying")
    void decliningDoesNothing() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        InkDissolver topCard = new InkDissolver();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, weavers, Keyword.FLYING)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new WaterspoutWeavers());
        harness.setLibrary(player1, List.of(new ElvishWarrior())); // Elf Warrior — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new WaterspoutWeavers());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship uses the source's last known creature types if it leaves before resolution")
    void sourceLeavingBeforeResolutionUsesLastKnownCreatureTypes() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        InkDissolver topCard = new InkDissolver();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(weavers);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, elf, Keyword.FLYING)).isTrue();
    }
}
