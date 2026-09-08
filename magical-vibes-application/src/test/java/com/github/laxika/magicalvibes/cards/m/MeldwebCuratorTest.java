package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeldwebCuratorTest extends BaseCardTest {

    private void castCurator() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MeldwebCurator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB offers only an instant or sorcery card and puts the choice on top")
    void putsChosenSpellOnTopOfLibrary() {
        Card filler = new GrizzlyBears();
        Card bolt = new LightningBolt();
        Card ponder = new Ponder();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(filler));
        harness.setGraveyard(player1, List.of(bolt, ponder, creature));

        castCurator();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(bolt.getId(), ponder.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ponder.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ponder, filler);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional ETB target can be declined")
    void canDeclineTarget() {
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));

        castCurator();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bolt);
    }

    @Test
    @DisplayName("No graveyard choice is offered when no instant or sorcery is present")
    void ignoresNonSpellCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castCurator();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
