package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EmpressGalina;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JodahTheUnifier.class, EmpressGalina.class, Forest.class, GrizzlyBears.class, TsaboTavoc.class})
class JodahTheUnifierDmuTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary creatures get +X/+X, including Jodah")
    void boostsLegendaryCreaturesByControlledLegendaryCreatureCount() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent otherLegend = addCreatureReady(player1, new TsaboTavoc());
        Permanent nonlegendary = addCreatureReady(player1, new GrizzlyBears());
        Permanent jodah = addCreatureReady(player1, new JodahTheUnifier());

        assertThat(gqs.getEffectivePower(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, otherLegend)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, otherLegend)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonlegendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonlegendary)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a legendary spell from hand exiles a lesser-mana-value legendary card")
    void castsLesserLegendaryCardFromLibraryForFree() {
        setupJodah();
        Card skipped = new TsaboTavoc();
        Card found = new EmpressGalina();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(skipped, found, remaining));
        harness.setHand(player1, List.of(new TsaboTavoc()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Empress Galina");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the free cast puts all exiled cards on the bottom")
    void declinedFoundCardReturnsToLibrary() {
        setupJodah();
        Card skipped = new Forest();
        Card found = new EmpressGalina();
        harness.setLibrary(player1, List.of(skipped, found));
        harness.setHand(player1, List.of(new TsaboTavoc()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(skipped, found);
    }

    @Test
    @DisplayName("Casting a nonlegendary spell does not trigger Jodah")
    void nonlegendarySpellDoesNotTrigger() {
        setupJodah();
        Card libraryCard = new TsaboTavoc();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    private void setupJodah() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JodahTheUnifier());
    }
}
