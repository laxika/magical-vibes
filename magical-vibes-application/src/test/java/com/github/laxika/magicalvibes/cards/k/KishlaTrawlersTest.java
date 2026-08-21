package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KishlaTrawlers.class, GrizzlyBears.class, Opt.class, ThinkTwice.class})
class KishlaTrawlersTest extends BaseCardTest {

    private void castKishlaTrawlers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KishlaTrawlers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles a creature and returns a chosen instant or sorcery")
    void exilesCreatureAndReturnsChosenSpell() {
        GrizzlyBears creature = new GrizzlyBears();
        Opt opt = new Opt();
        ThinkTwice thinkTwice = new ThinkTwice();
        harness.setGraveyard(player1, List.of(creature, opt, thinkTwice));

        castKishlaTrawlers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Grizzly Bears");
        harness.assertInHand(player1, "Think Twice");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertNotInGraveyard(player1, "Think Twice");
    }

    @Test
    @DisplayName("The optional exile can be declined")
    void exileCanBeDeclined() {
        GrizzlyBears creature = new GrizzlyBears();
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(creature, opt));

        castKishlaTrawlers();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertNotInHand(player1, "Opt");
    }

    @Test
    @DisplayName("Only creature cards can be exiled and only instants or sorceries can be returned")
    void filtersBothChoices() {
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(opt));

        castKishlaTrawlers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Opt");
    }
}
