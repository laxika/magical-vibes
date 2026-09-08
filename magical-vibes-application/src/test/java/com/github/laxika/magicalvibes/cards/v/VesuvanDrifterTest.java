package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanDrifter.class, GrizzlyBears.class, Island.class})
class VesuvanDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("May reveal a creature on top and copy it with flying")
    void copiesTopCreatureWithFlying() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(drifter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(drifter.getCard().getPower()).isEqualTo(2);
        assertThat(drifter.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A noncreature top card is revealed but is not copied")
    void doesNotCopyNoncreatureTopCard() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new Island()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(drifter.getCard().getName()).isEqualTo("Vesuvan Drifter");
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The temporary copy ends at cleanup")
    void copyEndsAtCleanup() {
        Permanent drifter = addDrifter();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(drifter.getCard().getName()).isEqualTo("Grizzly Bears");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(drifter.getCard().getName()).isEqualTo("Vesuvan Drifter");
        assertThat(drifter.getCard().getPower()).isEqualTo(2);
        assertThat(drifter.getCard().getToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, drifter, Keyword.FLYING)).isTrue();
    }

    private Permanent addDrifter() {
        return addCreatureReady(player1, new VesuvanDrifter());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
