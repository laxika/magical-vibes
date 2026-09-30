package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApexDevastator.class, Forest.class, GrizzlyBears.class})
class ApexDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Apex Devastator has four independent cascade triggers")
    void cascadesFourTimes() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));
        harness.setHand(player1, List.of(new ApexDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                    .params().cards())
                    .extracting("name")
                    .containsExactly("Grizzly Bears");

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        }

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
