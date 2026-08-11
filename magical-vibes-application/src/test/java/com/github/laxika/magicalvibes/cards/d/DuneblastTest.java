package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuneblastTest extends BaseCardTest {

    @Test
    void controllerCanKeepAnOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castDuneblast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DestroyRestChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player2, "Llanowar Elves")));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void choosingNoCreatureDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castDuneblast();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void withNoCreaturesThereIsNoChoice() {
        castDuneblast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Duneblast");
    }

    private void castDuneblast() {
        harness.setHand(player1, List.of(new Duneblast()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
