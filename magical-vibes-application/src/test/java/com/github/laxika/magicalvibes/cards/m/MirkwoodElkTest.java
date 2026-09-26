package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirkwoodElk.class, ElvishArchdruid.class, GrizzlyBears.class})
class MirkwoodElkTest extends BaseCardTest {

    @Test
    void entersAndReturnsAnElfThenGainsItsPower() {
        ElvishArchdruid elf = new ElvishArchdruid();
        GrizzlyBears nonElf = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(elf, nonElf));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MirkwoodElk()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(elf.getId());

        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Elvish Archdruid");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 12);
    }

    @Test
    void attacksAndReturnsAnElfThenGainsItsPower() {
        ElvishArchdruid elf = new ElvishArchdruid();
        addCreatureReady(player1, new MirkwoodElk());
        harness.setGraveyard(player1, List.of(elf));
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Elvish Archdruid");
        harness.assertLife(player1, 12);
    }
}
