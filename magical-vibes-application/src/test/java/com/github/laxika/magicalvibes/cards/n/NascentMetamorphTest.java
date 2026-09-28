package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NascentMetamorph.class, GrizzlyBears.class, Island.class})
class NascentMetamorphTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reveals an opponent's library and copies the found creature")
    void attacksAndCopiesFoundCreature() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card noncreature = new Island();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(noncreature, creature));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, metamorph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, metamorph)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(noncreature.getId(), creature.getId());
    }

    @Test
    @DisplayName("Blocking also triggers the library reveal and copy")
    void blocksAndCopiesFoundCreature() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature));
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("A missing creature leaves the Metamorph unchanged and bottoms all revealed cards")
    void noCreatureFound() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Nascent Metamorph");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("The copy ends at cleanup")
    void copyEndsAtCleanup() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();
        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Nascent Metamorph");
    }

    private void chooseOpponentTarget(Player chooser) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(chooser, player2.getId());
    }
}
